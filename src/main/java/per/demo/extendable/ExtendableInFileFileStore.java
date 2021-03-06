package per.demo.extendable;

import lombok.SneakyThrows;
import per.demo.InFileFileStore;
import per.demo.model.Configuration;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;
import per.demo.model.RebuildResult;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static per.demo.utils.MetaInfoUtils.*;
import static per.demo.validator.ExistingFileValidator.checkMetaDataLines;

public class ExtendableInFileFileStore implements InFileFileStore {

    private final String metaHeader;
    private final int metaHeaderBytesCount;
    private final String metaDelimiter;
    private int metaBytesCount;
    private int bufferSize;

    private Path file;
    private FileChannel channel;

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    private AtomicLong metaPos;

    @SneakyThrows
    public ExtendableInFileFileStore(Path file, Configuration configuration) {
        metaHeader = configuration.getMetaHeader();
        metaHeaderBytesCount = (metaHeader + "\n").getBytes(StandardCharsets.UTF_8).length;
        metaDelimiter = configuration.getMetaDelimiter();
        metaBytesCount = configuration.getMetaBytesCount();
        bufferSize = configuration.getBufferSize();

        boolean fileExists = false;
        if (!Files.exists(file)) {
            this.file = Files.createFile(file);
        } else {
            fileExists = true;
            this.file = file.getFileName();
        }

        this.channel = getFileChannel(this.file);

        if (!fileExists)
            initialize();
        else
            initializeFromFile();
    }

    @SneakyThrows
    @Override
    public synchronized List<FileInfo> saveContent(String fileName, String content) {
        long contentSize = content.getBytes(StandardCharsets.UTF_8).length;
        long startPos = channel.size();

        ByteBuffer buff = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(false);

        return addNewMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public synchronized List<FileInfo> saveContent(String fileName, InputStream contentStream) {
        byte[] buffer;
        long contentSize = 0;
        long startPos = channel.size();

        do {
            buffer = contentStream.readNBytes(bufferSize);

            if (buffer.length > 0) {
                ByteBuffer buff = ByteBuffer.wrap(buffer);

                channel.write(buff);
                contentSize += buffer.length;
            }
        } while (buffer.length > 0);

        channel.force(false);

        return addNewMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public synchronized List<FileInfo> saveContent(String fileName, ReadableByteChannel contentChannel) {
        long contentSize = 0;
        int lastReadCount;
        ByteBuffer buff = ByteBuffer.allocate(bufferSize);
        long startPos = channel.size();

        do {
            lastReadCount = contentChannel.read(buff);
            buff.flip();

            if (lastReadCount > 0) {
                channel.write(buff);
                buff.clear();

                contentSize += lastReadCount;
            }
        } while (lastReadCount > -1);

        channel.force(false);

        return addNewMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public String readContentString(long pos, long size) {
        return new String(readContentBytes(pos, size), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    @Override
    public byte[] readContentBytes(long pos, long size) {
        if (size <= 0) {
            return new byte[0];
        }

        int bufferSize;
        if (size > (long) Integer.MAX_VALUE) {
            bufferSize = this.bufferSize;
        } else {
            bufferSize = Math.min(this.bufferSize, (int) size);
        }

        ByteBuffer buff = ByteBuffer.allocate(bufferSize);
        long currentPos = pos;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (channel.read(buff, currentPos) > 0) {
                out.write(buff.array(), 0, buff.position());
                currentPos += buff.position();
                buff.clear();

                int remaining = (int) ((pos + size) - currentPos);
                if (remaining <= 0)
                    break;
                if (remaining < bufferSize) {
                    bufferSize = remaining;
                    buff = ByteBuffer.allocate(bufferSize);
                }
            }

            return out.toByteArray();
        }
    }

    @SneakyThrows
    @Override
    public synchronized void delete(long presentFlagPosition) {
        String content = "D";
        ByteBuffer wrap = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));

        channel.write(wrap, presentFlagPosition);
        channel.force(false);
    }

    @SneakyThrows
    @Override
    public void close() {
        synchronized (closeLock) {
            if (!open)
                return;

            channel.close();
            open = false;
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public String getFilePath() {
        return file.toString();
    }

    String getMetaContent() {
        return readContentString(metaHeaderBytesCount, metaBytesCount);
    }

    @SneakyThrows
    private void initialize() {
        String initialMetaContent = getInitialMetaContent(metaHeader, metaBytesCount, metaDelimiter);
        byte[] initialMetaContentBytes = initialMetaContent.getBytes(StandardCharsets.UTF_8);

        metaPos = new AtomicLong((metaHeader + "\n").getBytes(StandardCharsets.UTF_8).length);

        ByteBuffer buff = ByteBuffer.wrap(initialMetaContentBytes);
        channel.write(buff);
        channel.force(true);
    }

    @SneakyThrows
    private void initializeFromFile() {
        List<String> metaDataLines = Files.lines(file)
                .limit(3)
                .collect(Collectors.toList());
        checkMetaDataLines(metaDataLines, metaHeader, metaBytesCount, metaDelimiter);

        String metaContent = metaDataLines.get(1);

        metaBytesCount = metaContent.getBytes(StandardCharsets.UTF_8).length;

        int initialMetaPos = (metaHeader + "\n" + metaContent.trim()).getBytes(StandardCharsets.UTF_8).length;
        metaPos = new AtomicLong(initialMetaPos);
    }

    @SneakyThrows
    private List<FileInfo> addNewMeta(String fileName, long contentLength, long startPos) {
        byte[] newMetaContentBytes = buildMetaContent(fileName, contentLength, startPos);
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();

        if (needToIncreaseMeta(newMetaContentBytes.length)) {
            FileInfo newAddingFile = new FileInfo(fileName, new MetaInfo(startPos, contentLength));

            do {
                fileInfosToUpdate = rebuild(newAddingFile);
                newAddingFile = fileInfosToUpdate.get(fileInfosToUpdate.size() - 1);

                newMetaContentBytes = buildMetaContent(newAddingFile);
            } while (needToIncreaseMeta(newMetaContentBytes.length));
        } else {
            fileInfosToUpdate.add(getNewFileInfoToUpdate(fileName, contentLength, startPos));
        }

        writeMeta(newMetaContentBytes);

        return fileInfosToUpdate;
    }

    private List<FileInfo> rebuild(FileInfo newAddingFile) {
        InFileFileStoreRebuildService rebuildService =
                new InFileFileStoreRebuildService(this.metaHeader, this.metaDelimiter, this.file, this.channel);

        String oldMeta = readContentString(metaHeaderBytesCount, metaBytesCount);
        RebuildResult rebuildResult = rebuildService
                .increaseMetaSpaceAndRebuild(this.metaBytesCount, oldMeta, newAddingFile);

        this.file = rebuildResult.getFile();
        this.channel = rebuildResult.getChannel();
        this.metaPos = rebuildResult.getMetaPos();
        this.metaBytesCount = rebuildResult.getMetaBytesCount();

        return rebuildResult.getFileInfosToUpdate();
    }

    @SneakyThrows
    private void writeMeta(byte[] metaContentBytes) {
        ByteBuffer buff = ByteBuffer.wrap(metaContentBytes);

        channel.write(buff, metaPos.get());
        channel.force(false);

        metaPos.getAndAdd(metaContentBytes.length);
    }

    @SneakyThrows
    private FileInfo getNewFileInfoToUpdate(String fileName, long contentLength, long startPos) {
        long presentFlagPosition = metaPos.get() - 2;

        return new FileInfo(fileName, new MetaInfo(startPos, contentLength, presentFlagPosition));
    }

    private byte[] buildMetaContent(FileInfo fileInfo) {
        MetaInfo metaInfo = fileInfo.getMetaInfo();

        return buildMetaContent(fileInfo.getName(), metaInfo.getSize(), metaInfo.getStartPosition());
    }

    private byte[] buildMetaContent(String fileName, long contentLength, long startPos) {
        String metaContent = "{\"" + fileName + "\"," +
                startPos + "," +
                contentLength +
                ",A}";

        return metaContent.getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private FileChannel getFileChannel(Path file) {
        return FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
    }

    private boolean needToIncreaseMeta(long metaContentSize) {
        return needToIncreaseMetaSpace(metaHeaderBytesCount, metaBytesCount, metaContentSize, metaPos.get());
    }
}

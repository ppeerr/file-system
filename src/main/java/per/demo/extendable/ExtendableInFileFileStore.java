package per.demo.extendable;

import lombok.SneakyThrows;
import per.demo.InFileFileStore;
import per.demo.model.Configuration;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static per.demo.utils.MetaInfoUtils.*;
import static per.demo.validator.ExistingFileValidator.checkMetaDataLines;

public class ExtendableInFileFileStore implements InFileFileStore {

    private static final String BUF_FILE_SUFFIX = ".buf";
    private static final String BUF_FILE_SUFFIX_META = ".buf_meta";
    private static final int BUFFER_SIZE = 1024;

    private final String metaHeader;
    private final int metaHeaderBytesCount;
    private final String metaDelimiter;
    private int metaBytesCount;

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

        return addMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public synchronized List<FileInfo> saveContent(String fileName, InputStream contentStream) {
        byte[] buffer;
        long contentSize = 0;
        long startPos = channel.size();

        do {
            buffer = contentStream.readNBytes(BUFFER_SIZE);

            if (buffer.length > 0) {
                ByteBuffer buff = ByteBuffer.wrap(buffer);

                channel.write(buff);
                contentSize += buffer.length;
            }
        } while (buffer.length > 0);

        channel.force(false);

        return addMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public synchronized List<FileInfo> saveContent(String fileName, ReadableByteChannel contentChannel) {
        long contentSize = 0;
        int lastReadCount;
        ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);
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

        return addMeta(fileName, contentSize, startPos);
    }

    @SneakyThrows
    @Override
    public String readContent(long pos, long size) {
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
            bufferSize = BUFFER_SIZE;
        } else {
            bufferSize = Math.min(BUFFER_SIZE, (int) size);
        }

        ByteBuffer buff = ByteBuffer.allocate(bufferSize);
        long currentPos = pos;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (channel.read(buff, currentPos) > 0) {
                out.write(buff.array(), 0, buff.position());
                currentPos += buff.position(); //TODO check
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
        return readContent(metaHeaderBytesCount, metaBytesCount);
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
    private List<FileInfo> addMeta(String fileName, long contentLength, long startPos) {
        byte[] newFileMetaContentBytes = buildMetaContent(fileName, contentLength, startPos);
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();

        if (needToIncreaseMeta(newFileMetaContentBytes.length)) {
            FileInfo newFile = new FileInfo(fileName, new MetaInfo(startPos, contentLength, -1));
            do {
                fileInfosToUpdate = new ArrayList<>(increaseMetaSpaceAndRebuild(newFile));
                newFile = fileInfosToUpdate.get(fileInfosToUpdate.size() - 1);

                newFileMetaContentBytes = buildMetaContent(newFile.getName(), newFile.getMetaInfo().getSize(), newFile.getMetaInfo().getStartPosition());
            } while (needToIncreaseMeta(newFileMetaContentBytes.length));
        } else {
            fileInfosToUpdate.add(getNewFileInfoToUpdate(fileName, contentLength, startPos));
        }

        writeMeta(newFileMetaContentBytes);

        return fileInfosToUpdate;
    }

    @SneakyThrows
    private List<FileInfo> increaseMetaSpaceAndRebuild(FileInfo newFileInfo) {
        int newMetaBytesCount = metaBytesCount << 1;
        int newMetaPos = (metaHeader + "\n").getBytes(StandardCharsets.UTF_8).length;

        Path bufFile = Files.createFile(Paths.get(file.toString() + BUF_FILE_SUFFIX));
        FileChannel bufChannel = getFileChannel(bufFile);

        writeInitialMetaState(newMetaBytesCount, bufChannel);

        List<FileInfo> oldFileInfos = getOldFileInfos();
        oldFileInfos.add(newFileInfo);

        long presentFlagPos = newMetaPos;
        ByteBuffer buff;
        List<String> metaInfosToStore = new ArrayList<>();
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();
        for (int i = 0; i < oldFileInfos.size(); i++) {
            FileInfo metaElement = oldFileInfos.get(i);

            MetaInfo metaInfo = metaElement.getMetaInfo();

            byte[] contentBytes = readContent(metaInfo.getStartPosition(), metaInfo.getSize())
                    .getBytes(StandardCharsets.UTF_8);
            buff = ByteBuffer.wrap(contentBytes);

            long newStart = bufChannel.position();
            bufChannel.write(buff);

            String currentNewFileInfoElement = "{\"" + metaElement.getName() + "\"," + newStart + "," + metaInfo.getSize() + "," + "A}";
            if (i != oldFileInfos.size() - 1) {
                metaInfosToStore.add(currentNewFileInfoElement);
            }

            presentFlagPos += currentNewFileInfoElement
                    .substring(0, currentNewFileInfoElement.length() - 2).getBytes(StandardCharsets.UTF_8).length;

            fileInfosToUpdate.add(new FileInfo(
                    metaElement.getName(),
                    new MetaInfo(
                            newStart,
                            metaInfo.getSize(),
                            presentFlagPos
                    )
            ));

            presentFlagPos += currentNewFileInfoElement
                    .substring(currentNewFileInfoElement.length() - 2).getBytes(StandardCharsets.UTF_8).length;
        }
        String metaContentToStore = String.join("", metaInfosToStore);
        buff = ByteBuffer.wrap((metaContentToStore).getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff, newMetaPos);
        bufChannel.force(false);

        this.channel.close();

        long newEndPos = bufChannel.size();
        bufChannel.close();

        Files.move(bufFile, Paths.get(this.file.toString()), REPLACE_EXISTING);

        this.file = Paths.get(this.file.toString());
        this.channel = getFileChannel(this.file)
                .position(newEndPos);

        metaPos = new AtomicLong(newMetaPos + metaContentToStore.length());
        metaBytesCount = newMetaBytesCount;

        return fileInfosToUpdate;
    }

    private List<FileInfo> getOldFileInfos() {
        String oldMeta = readContent(metaHeaderBytesCount, metaBytesCount);

        return getMetaDataElements(oldMeta).stream()
                .filter(withPresentState())
                .map(metaElement -> {
                    String[] metaElementParts = metaElement.split(",");

                    String fileName = metaElementParts[0].substring(2, metaElementParts[0].length() - 1);
                    long start = Long.parseLong(metaElementParts[1]);
                    long size = Integer.parseInt(metaElementParts[2]);

                    return new FileInfo(
                            fileName,
                            new MetaInfo(start, size, -1)
                    );
                })
                .collect(Collectors.toList());
    }

    private void writeInitialMetaState(int newMetaBytesCount, FileChannel bufChannel) throws IOException {
        String newInitialStartState = getInitialMetaContent(metaHeader, newMetaBytesCount, metaDelimiter);

        ByteBuffer buff = ByteBuffer.wrap(newInitialStartState.getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff);
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

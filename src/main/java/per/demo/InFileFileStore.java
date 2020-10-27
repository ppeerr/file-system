package per.demo;

import lombok.SneakyThrows;
import per.demo.model.Configuration;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static per.demo.utils.MetaInfoUtils.getInitialMetaContent;
import static per.demo.utils.MetaInfoUtils.needToIncreaseMetaSpace;
import static per.demo.validator.ExistingFileValidator.checkMetaDataLines;

class InFileFileStore {

    private final String metaHeader;
    private final int metaHeaderBytesCount;
    private final String metaDelimiter;
    private int metaBytesCount;

    private Path file;
    private FileChannel channel;

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    private AtomicLong metaPos;
    private volatile long endPos;

    @SneakyThrows
    InFileFileStore(String fileName, Configuration configuration) {
        metaHeader = configuration.getMetaHeader();
        metaHeaderBytesCount = (metaHeader + "\n").getBytes().length;
        metaDelimiter = configuration.getMetaDelimiter();
        metaBytesCount = configuration.getMetaBytesCount();

        Path file = Paths.get(fileName);
        boolean fileExists = false;
        if (!Files.exists(file)) {
            this.file = Files.createFile(file);
        } else {
            fileExists = true;
            this.file = file;
        }

        this.channel = getFileChannel(this.file);

        if (!fileExists)
            initialize();
        else
            initializeFromFile();
    }

    @SneakyThrows
    synchronized List<FileInfo> saveContent(String fileName, String content) {
        List<FileInfo> fileInfoToUpdates = addMeta(fileName, content);

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(false);
        endPos = channel.size();

        return fileInfoToUpdates;
    }

    @SneakyThrows
    String readContent(long pos, int size) {
        ByteBuffer buff = ByteBuffer.allocate(size);
        channel.read(buff, pos);

        return new String(buff.array(), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    synchronized void setDeletedMetaFlag(long presentFlagPosition) {
        String content = "D";
        ByteBuffer wrap = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));

        channel.write(wrap, presentFlagPosition);
        channel.force(false);
    }

    String getMetaContent() {
        return readContent(metaHeaderBytesCount, metaBytesCount);
    }

    @SneakyThrows
    void close() {
        synchronized (closeLock) {
            if (!open)
                return;

            channel.close();
            open = false;
        }
    }

    boolean isOpen() {
        return open;
    }

    String getFilePath() {
        return file.toString();
    }

    @SneakyThrows
    private void initialize() {
        String initialMetaContent = getInitialMetaContent(metaHeader, metaBytesCount, metaDelimiter);
        byte[] initialMetaContentBytes = initialMetaContent.getBytes(StandardCharsets.UTF_8);

        metaPos = new AtomicLong((metaHeader + "\n").getBytes().length);
        endPos = initialMetaContentBytes.length;

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

        metaBytesCount = metaContent.getBytes().length;

        int initialMetaPos = (metaHeader + "\n" + metaContent.trim()).getBytes().length;
        metaPos = new AtomicLong(initialMetaPos);
    }

    @SneakyThrows
    private List<FileInfo> addMeta(String fileName, String content) {
        byte[] contentBytes = content.getBytes();
        byte[] metaContentBytes = buildMetaContent(fileName, contentBytes);
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();

        while (needToIncreaseMetaSpace(metaHeaderBytesCount, metaBytesCount, metaContentBytes.length, metaPos.get())) {
            fileInfosToUpdate = new ArrayList<>(rebuildAndIncreaseMetaSpace());
            metaContentBytes = buildMetaContent(fileName, contentBytes);
        }

        ByteBuffer buff = ByteBuffer.wrap(metaContentBytes);
        channel.write(buff, metaPos.get());
        channel.force(false);

        long start = channel.size();
        int size = contentBytes.length;

        long isPresentPosition = metaPos.get() + metaContentBytes.length - 2;

        metaPos.getAndAdd(metaContentBytes.length);

        fileInfosToUpdate.add(new FileInfo(fileName, new MetaInfo(start, size, isPresentPosition)));
        return fileInfosToUpdate;
    }

    @SneakyThrows
    private synchronized List<FileInfo> rebuildAndIncreaseMetaSpace() {
        String bufFileName = file.toString() + ".buf";
        Path bufFile = Paths.get(bufFileName);

        bufFile = Files.createFile(bufFile);
        FileChannel bufChannel = getFileChannel(bufFile);

        int newMetaBytes = metaBytesCount << 1;
        String newStartState = getInitialMetaContent(metaHeader, newMetaBytes, metaDelimiter);

        int newMetaPos = (metaHeader + "\n").getBytes().length;

        ByteBuffer buff = ByteBuffer.wrap(newStartState.getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff);

        String oldMeta = readContent(metaHeaderBytesCount, metaBytesCount);
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern
                .compile("\\{([^}]+)}")
                .matcher(oldMeta);
        while (m.find()) {
            allMatches.add(m.group(0));
        }

        List<String> metaInfos = allMatches.stream()
                .filter(info -> {
                    String state = info.substring(info.length() - 2, info.length() - 1);
                    return state.equals("A");
                })
                .collect(Collectors.toList());

        List<String> metaInfosToStore = new ArrayList<>();
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();
        long isPresentFlagPos = newMetaPos;
        for (String info : metaInfos) {
            String[] mas = info.split(",");

            String fileName = mas[0].substring(2, mas[0].length() - 1);
            long oldStart = Long.parseLong(mas[1]);
            int size = Integer.parseInt(mas[2]);
            String state = mas[3].substring(0, mas[3].length() - 1);

            String content = readContent(oldStart, size) + "\n";
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            buff = ByteBuffer.wrap(contentBytes);

            long newStart = bufChannel.position();
            bufChannel.write(buff);

            String curFileMeta = "{\"" + fileName + "\"," + newStart + "," + size + "," + state + "}";
            metaInfosToStore.add(curFileMeta);

            isPresentFlagPos += curFileMeta.substring(0, curFileMeta.length() - 2).getBytes().length;

            fileInfosToUpdate.add(new FileInfo(
                    fileName,
                    new MetaInfo(
                            newStart,
                            size,
                            isPresentFlagPos
                    )
            ));

            isPresentFlagPos += curFileMeta.substring(curFileMeta.length() - 2).getBytes().length;
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
        endPos = newEndPos;
        metaBytesCount = newMetaBytes;

        return fileInfosToUpdate;
    }

    private byte[] buildMetaContent(String fileName, byte[] contentBytes) {
        String metaContent = "{\"" + fileName + "\"," +
                endPos + "," +
                contentBytes.length +
                ",A}";

        return metaContent.getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private FileChannel getFileChannel(Path file) {
        return FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
    }
}

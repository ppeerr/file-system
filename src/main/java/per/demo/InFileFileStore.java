package per.demo;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
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

class InFileFileStore { //Extends FileStore

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

    synchronized List<FileInfo> saveContent(String fileName, String content) throws IOException {
        List<FileInfo> fileInfoToUpdates = addMeta(fileName, content);

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(false);
        endPos = channel.size();

        return fileInfoToUpdates;
    }

    String readContent(long pos, int size) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(size); //TODO refactor
        channel.read(buff, pos);

        return new String(buff.array(), StandardCharsets.UTF_8);
    }

    synchronized void setDeletedMetaFlag(long presentFlagPosition) throws IOException {
        String content = "D";
        ByteBuffer wrap = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));

        channel.write(wrap, presentFlagPosition);
        channel.force(false);
    }

    String getMetaContent() throws IOException {
        return readContent(metaHeaderBytesCount, metaBytesCount);
    }

    void close() throws IOException {
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

    private void initialize() throws IOException {
        String startMetaContent = getStartMetaContent(metaBytesCount);

        metaPos = new AtomicLong((metaHeader + "\n").getBytes().length);
        endPos = startMetaContent.getBytes().length;

        ByteBuffer buff = ByteBuffer.wrap(startMetaContent.getBytes(StandardCharsets.UTF_8));
        channel.write(buff);
        channel.force(true);
    }

    private String getStartMetaContent(int metaBytesCount) {
        return metaHeader + "\n" +
                StringUtils.repeat(' ', metaBytesCount) + "\n" +
                metaDelimiter + "\n";
    }

    private void initializeFromFile() throws IOException {
        //todo validation
        String metaContent = Files.readAllLines(file).get(1); //TODO optimize

        metaBytesCount = metaContent.getBytes().length;
        metaPos = new AtomicLong((metaHeader + "\n" + metaContent.trim()).getBytes().length);
    }

    private List<FileInfo> addMeta(String fileName, String content) throws IOException {
        byte[] metaContent = buildMetaContent(fileName, content);
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();

        if (needToIncreaseMetaSpace(metaContent.length)) {
            fileInfosToUpdate.addAll(rebuildAndIncreaseMetaSpace());
            metaContent = buildMetaContent(fileName, content);
        }

        ByteBuffer buff = ByteBuffer.wrap(metaContent);
        channel.write(buff, metaPos.get());
        channel.force(true);

        long start = channel.size();
        int size = content.getBytes().length;

        long isPresentPosition = metaPos.get() + metaContent.length - 2;

        metaPos.getAndAdd(metaContent.length);

        fileInfosToUpdate.add(new FileInfo(fileName, new MetaInfo(start, size, isPresentPosition)));
        return fileInfosToUpdate;
    }

    private synchronized List<FileInfo> rebuildAndIncreaseMetaSpace() throws IOException { //TODO synchronized?
        String bufFileName = file.toString() + ".buf";
        Path bufFile = Paths.get(bufFileName);

        bufFile = Files.createFile(bufFile);
        FileChannel bufChannel = getFileChannel(bufFile);

        int newMetaBytes = metaBytesCount << 1;
        String newStartState = getStartMetaContent(newMetaBytes);

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

    private byte[] buildMetaContent(String fileName, String content) {
        String metaContent = "{\"" + fileName + "\"," +
                endPos + "," +
                content.getBytes().length +
                ",A}";

        return metaContent.getBytes(StandardCharsets.UTF_8);
    }

    private boolean needToIncreaseMetaSpace(int metaContentSize) {
        int lastPossibleMetaPos = metaHeaderBytesCount + metaBytesCount;

        return metaPos.get() + metaContentSize > lastPossibleMetaPos;
    }

    private FileChannel getFileChannel(Path file) throws IOException {
        return FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
    }
}

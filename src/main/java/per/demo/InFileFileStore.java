package per.demo;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        this.channel = getFileChannel(this.file); //TODO check

//    channel = new RandomAccessFile(this.file.toFile(), "rwd").getChannel(); //TODO check

        if (!fileExists)
            initialize();
        else
            initializeFromFile();
    }

    synchronized MetaInfo saveContent(String fileName, String content) throws IOException {
        MetaInfo metaInfo = addMeta(fileName, content);

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(false);
        endPos = channel.size();

        return metaInfo;
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

    synchronized String getMetaContent() throws IOException { //TODO synchronized?
        return readContent(metaHeaderBytesCount, metaBytesCount);
    }

    void destroy() throws IOException {
        synchronized (closeLock) {
            if (!open)
                return;

            channel.close();
            Files.delete(file); //TODO needed?
            open = false;
        }
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

        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern
                .compile("\\{([^}]+)}")
                .matcher(metaContent);
        while (m.find()) {
            allMatches.add(m.group(1));
        }

        metaBytesCount = metaContent.getBytes().length;
        metaPos = new AtomicLong((metaHeader + "\n" + metaContent.trim()).getBytes().length);
    }

    private MetaInfo addMeta(String fileName, String content) throws IOException {
        byte[] metaContent = buildMetaContent(fileName, content);

        if (needToIncreaseMetaSpace(metaContent.length)) {
            rebuildAndIncreaseMetaSpace();
        }

        ByteBuffer buff = ByteBuffer.wrap(metaContent);
        channel.write(buff, metaPos.get());
        channel.force(true);

        long start = channel.size();
        int size = content.getBytes().length;

        long isPresentPosition = metaPos.get() + metaContent.length - 2;

        metaPos.getAndAdd(metaContent.length);

        return new MetaInfo(start, size, isPresentPosition);
    }

    private synchronized void rebuildAndIncreaseMetaSpace() throws IOException { //TODO synchronized?
        String bufFileName = file.toString() + ".buf";
        Path bufFile = Paths.get(bufFileName);

        FileChannel bufChannel = getFileChannel(bufFile);

        int newMetaBytes = metaBytesCount << 1;
        String newStartState = getStartMetaContent(newMetaBytes);

        int newMetaPos = (metaHeader + "\n").getBytes().length;
        long newEndPos = newStartState.getBytes().length;

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

        List<String> metaInfosToStore = allMatches.stream()
                .filter(info -> {
                    String state = info.substring(info.length() - 2, info.length() - 1);
                    return state.equals("A");
                })
                .collect(Collectors.toList());

        for (String info : metaInfosToStore) {
            String[] mas = info.split(",");

            long start = Long.parseLong(mas[1]);
            int size = Integer.parseInt(mas[2]);

            String content = readContent(start, size) + "\n";
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            buff = ByteBuffer.wrap(contentBytes);
            bufChannel.write(buff);

            newEndPos += contentBytes.length;
        }

        String metaContentToStore = String.join("", metaInfosToStore);
        buff = ByteBuffer.wrap((metaContentToStore).getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff, newMetaPos);
        bufChannel.force(false);

        this.channel.close();
        bufChannel.close();

        Files.move(bufFile, Paths.get(this.file.toString()), REPLACE_EXISTING);

        this.file = Paths.get(this.file.toString());
        this.channel = getFileChannel(this.file)
                .position(newEndPos);

        metaPos = new AtomicLong(metaContentToStore.length());
        endPos = newEndPos;
        metaBytesCount = newMetaBytes;
    }

    private byte[] buildMetaContent(String fileName, String content) {
        StringBuilder metaContent = new StringBuilder();
//        if (!positionsAndSizesByNames.isEmpty()) {
//            metaContent.append(";");
//        }

        metaContent
                .append("{\"").append(fileName).append("\",")
                .append(endPos).append(",")
                .append(content.getBytes().length)
                .append(",A}");

        return metaContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private boolean needToIncreaseMetaSpace(int metaContentSize) {
        int lastPossibleMetaPos = metaHeaderBytesCount + metaBytesCount;

        return metaPos.get() + metaContentSize > lastPossibleMetaPos;
    }

    private FileChannel getFileChannel(Path file) throws IOException {
        return FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
    }
}

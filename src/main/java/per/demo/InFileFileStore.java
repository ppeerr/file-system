package per.demo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class InFileFileStore { //Extends FileStore

    private static final String HEADER = "START";
    private static final int HEADER_BYTES = (HEADER + "\n").getBytes().length;
    private static final String META_DELIMITER = "----META ENDS----";

    private Path file;
    @Getter(AccessLevel.PACKAGE)
    private ConcurrentMap<String, Triple<Long, Integer, Boolean>> positionsAndSizesByNames = new ConcurrentHashMap<>();
    private FileChannel channel;
    private RandomAccessFile fin;

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    private int metaBytes;

    private volatile AtomicLong metaPos;
    private volatile long endPos;

    @SneakyThrows
    InFileFileStore(String fileName) {
        Path p = Paths.get(fileName); //needed?

        boolean fileExists = false;
        if (!Files.exists(p)) {
            file = Files.createFile(p);
        } else {
            fileExists = true;
            file = p;
        }

        fin = new RandomAccessFile(p.toFile(), "rwd");
        channel = fin.getChannel();
        if (channel == null) {
            throw new RuntimeException("Couldn't open channel");
        }

        if (!fileExists)
            initialize();
        else
            initializeFromFile();
    }

    private void initialize() throws IOException {
        metaBytes = 100;

        String startState = HEADER + "\n" + StringUtils.repeat(' ', metaBytes) + "\n" + META_DELIMITER + "\n"; //TODO optimize?
        metaPos = new AtomicLong((HEADER + "\n").getBytes().length);
        endPos = startState.getBytes().length;

        ByteBuffer buff = ByteBuffer.wrap(startState.getBytes(StandardCharsets.UTF_8));
        channel.write(buff);
        channel.force(true);
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

        allMatches.stream()
                .map(it -> it.split(","))
                .forEach(mas -> {
                    String name = mas[0].substring(1, mas[0].length() - 1);
                    long start = Long.parseLong(mas[1]);
                    int size = Integer.parseInt(mas[2]);
                    String state = mas[3];

                    if (state.equals("A"))
                        positionsAndSizesByNames.put(name, Triple.of(start, size, true));
                });

        metaBytes = metaContent.getBytes().length;
        metaPos = new AtomicLong((HEADER + "\n" + metaContent.trim()).getBytes().length);
    }

    synchronized void addContent(String fileName, String content) throws IOException { //TODO check sync
        addMeta(fileName, content);

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(true);
        endPos = channel.size();
    }

    private void addMeta(String fileName, String content) throws IOException {
        byte[] metaContent = buildMetaContent(fileName, content);

        if (needToIncreaseMetaSpace(metaContent.length)) {
            rebuildAndIncreaseMetaSpace();
        }

        ByteBuffer buff = ByteBuffer.wrap(metaContent);
        channel.write(buff, metaPos.get());
        channel.force(true);
        metaPos.getAndAdd(metaContent.length);

        positionsAndSizesByNames.put(
                fileName,
                Triple.of(
                        channel.size(),
                        content.getBytes().length,
                        true
                )
        );
    }

    private synchronized void rebuildAndIncreaseMetaSpace() throws IOException { //TODO synchronized?
        String bufFileName = file.toString() + ".buf";
        Path p = Paths.get(bufFileName);

        RandomAccessFile fin = new RandomAccessFile(p.toFile(), "rwd");
        FileChannel channel = fin.getChannel();
        if (channel == null) {
            throw new RuntimeException("Couldn't open channel");
        }

        ConcurrentMap<String, Triple<Long, Integer, Boolean>> newMap = positionsAndSizesByNames.entrySet().stream()
                .filter(entry -> entry.getValue().getRight())
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));

        StringBuilder metaContent = new StringBuilder("");

        int newMetaBytes = metaBytes << 1;
        String newStartState = HEADER + "\n" + StringUtils.repeat(' ', newMetaBytes) + "\n" + META_DELIMITER + "\n"; //TODO optimize?
        int newMetaPos = (HEADER + "\n").getBytes().length;
        long newEndPos = newStartState.getBytes().length;

        ByteBuffer buff = ByteBuffer.wrap(newStartState.getBytes(StandardCharsets.UTF_8));
        channel.write(buff);

        for (Map.Entry<String, Triple<Long, Integer, Boolean>> entry : newMap.entrySet()) {
            String fileName = entry.getKey();
            int size = entry.getValue().getMiddle();

            metaContent
                    .append("{\"").append(fileName).append("\",")
                    .append(newEndPos).append(",")
                    .append(size)
                    .append(",A};");

            String content = read(fileName) + "\n";
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            buff = ByteBuffer.wrap(contentBytes);

            channel.write(buff);

            newEndPos += contentBytes.length;
        }

        String metaContentToStore = metaContent.toString().substring(0, metaContent.length() - 1);
        buff = ByteBuffer.wrap((metaContentToStore).getBytes(StandardCharsets.UTF_8));

        channel.write(buff, newMetaPos);
        channel.force(true);

        this.channel.close();
        this.fin.close();

        channel.close();
        fin.close();

        Files.move(p, Paths.get(file.toString()), REPLACE_EXISTING);

        file = Paths.get(file.toString());
        this.fin = new RandomAccessFile(file.toFile(), "rwd");
        this.channel = this.fin.getChannel();
        if (this.channel == null) {
            throw new RuntimeException("Couldn't open channel");
        }
        this.channel.position(newEndPos);

        positionsAndSizesByNames = newMap;
        open = true;
        metaPos = new AtomicLong(metaContentToStore.length());
        endPos = newEndPos;
        metaBytes = newMetaBytes;
    }

    private byte[] buildMetaContent(String fileName, String content) {
        StringBuilder metaContent = new StringBuilder("");
        if (!positionsAndSizesByNames.isEmpty()) {
            metaContent.append(";");
        }

        metaContent
                .append("{\"").append(fileName).append("\",")
                .append(endPos).append(",")
                .append(content.getBytes().length)
                .append(",A}");

        return metaContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private boolean needToIncreaseMetaSpace(int metaContentSize) {
        int lastPossibleMetaBytePos = HEADER_BYTES + metaBytes;

        return metaPos.get() + metaContentSize > lastPossibleMetaBytePos;
    }

    String read(String fileName) throws IOException {
        if (!positionsAndSizesByNames.containsKey(fileName)) {
            throw new RuntimeException("No file " + fileName + " found");
        }

        Triple<Long, Integer, Boolean> info = positionsAndSizesByNames.get(fileName);
        Long pos = info.getLeft();
        Integer size = info.getMiddle();
        String fileContent;

        ByteBuffer buff = ByteBuffer.allocate(size); //TODO refactor
        channel.read(buff, pos);
        fileContent = new String(buff.array(), StandardCharsets.UTF_8);

        return fileContent;
    }

    void delete(String fileName) { //TODO sync with meta
        positionsAndSizesByNames.remove(fileName);
    }

    void destroy() throws IOException {
        synchronized (closeLock) {
            if (!open)
                return;

            channel.close();
            fin.close();
            Files.delete(file);
            open = false;
        }
    }
}

package per.demo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class InFileFileStore { //Extends FileStore

    private static final String HEADER = "START";
    private static final String META_DELIMITER = "----META ENDS----";

    private Path file;
    @Getter(AccessLevel.PACKAGE)
    private ConcurrentHashMap<String, Pair<Long, Integer>> positionsAndSizesByNames = new ConcurrentHashMap<>();
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

        fin = new RandomAccessFile(p.getFileName().toFile(), "rwd");

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
        metaBytes = 2000;

        String startState = HEADER + "\n" + StringUtils.repeat(' ', metaBytes) + "\n" + META_DELIMITER + "\n";
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
                        positionsAndSizesByNames.put(name, new Pair<>(start, size));
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
        StringBuilder metaContent = new StringBuilder("");
        if (!positionsAndSizesByNames.isEmpty()) {
            metaContent.append(";");
        }

        metaContent
                .append("{\"").append(fileName).append("\",")
                .append(endPos).append(",")
                .append(content.getBytes().length)
                .append(",A}");

        byte[] meta = metaContent.toString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buff = ByteBuffer.wrap(meta);
        channel.write(buff, metaPos.get());
        channel.force(true);
        metaPos.getAndAdd(meta.length);

        positionsAndSizesByNames.put(
                fileName,
                new Pair<>(
                        channel.size(),
                        content.getBytes().length
                )
        );
    }

    String read(String fileName) throws IOException {
        if (!positionsAndSizesByNames.containsKey(fileName)) {
            throw new RuntimeException("No file " + fileName + " found");
        }

        Pair<Long, Integer> info = positionsAndSizesByNames.get(fileName);
        Long pos = info.getFirst();
        Integer size = info.getSecond();
        String fileContent;

        ByteBuffer buff = ByteBuffer.allocate(size); //TODO refactor
        channel.read(buff, pos);
        fileContent = new String(buff.array(), StandardCharsets.UTF_8);

        return fileContent;
    }

    void delete(String fileName) {
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

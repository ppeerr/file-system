package per.demo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.math3.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

class InFileFileStore { //Extends FileStore

    private Path file;
    @Getter(AccessLevel.PACKAGE)
    private ConcurrentHashMap<String, Pair<Long, Integer>> positionsAndSizesByNames = new ConcurrentHashMap<>();
    private FileChannel channel;
    private RandomAccessFile fin;

    private final Object closeLock = new Object();
    private volatile boolean open = true;

    @SneakyThrows
    InFileFileStore(String fileName) {
        Path p = Paths.get(fileName); //needed?

        if (!Files.exists(p)) {
            file = Files.createFile(p);
        } else {
            file = p;
        }

        try {
            fin = new RandomAccessFile(p.getFileName().toFile(), "rwd");
            channel = fin.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (channel == null) {
            throw new RuntimeException("Couldn't open channel");
        }
    }

    synchronized void addContent(String fileName, String content) throws IOException { //TODO check sync
        positionsAndSizesByNames.put(
                fileName,
                new Pair<>(
                        channel.size(),
                        content.getBytes().length
                )
        );

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));

        channel.write(buff);
        channel.force(true);
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

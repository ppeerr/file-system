package per.demo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InFileFileStore { //Extends FileStore
    private String name;
    private Path file;
    private List<String> fileNames = new ArrayList<>();
    private Map<String, Long> positionsByNames = new HashMap<>();
    private Map<String, Integer> sizesByNames = new HashMap<>();
    private FileChannel channel;
    private RandomAccessFile fin;

    InFileFileStore(String name) {
        this.name = name;

        String fileName = name + ".txt";
        Path p = Paths.get(fileName);

        if (Files.exists(p)) {
            throw new RuntimeException("File already exists");
        }

        try {
            file = Files.createFile(p);
        } catch (IOException e) {
            e.printStackTrace();
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
        fileNames.add(fileName);
        positionsByNames.put(fileName, channel.size());
        sizesByNames.put(fileName, content.getBytes().length);

        ByteBuffer buff = ByteBuffer.wrap((content + "\n").getBytes(StandardCharsets.UTF_8));


        channel.write(buff);
        channel.force(true);
    }

    String read(String fileName) throws IOException {
        if (!fileNames.contains(fileName)) {
            throw new RuntimeException("No file " + fileName + " found");
        }

        Long pos = positionsByNames.get(fileName);
        Integer size = sizesByNames.get(fileName);
        String fileContent;

        ByteBuffer buff = ByteBuffer.allocate(size); //TODO refactor
        int noOfBytesRead = channel.read(buff, pos);
        fileContent = new String(buff.array(), StandardCharsets.UTF_8);

        return fileContent;
    }

    void delete(String fileName) {
        fileNames.remove(fileName);
        positionsByNames.remove(fileName);
        sizesByNames.remove(fileName);
    }

    void destroy() throws IOException {
        channel.close();
        fin.close();
        Files.delete(file);
    }

    List<String> getFileNames() {
        return fileNames;
    }

    String getName() {
        return name;
    }
}

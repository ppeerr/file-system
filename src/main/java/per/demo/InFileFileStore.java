package per.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InFileFileStore { //Extends FileStore
    private String name;
    private Path file;
    private List<String> fileNames = new ArrayList<>();

    public InFileFileStore(String name) {
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
    }

    public void add(String fileName) {
        fileNames.add(fileName);
    }

    public synchronized void addContent(String fileName, String content) { //TODO check sync
        FileInputStream fin= new FileInputStream(file.getName());
        FileChannel channel = fin.getChannel();
        try {
            OutputStream outputStream = Files.newOutputStream(file);

            outputStream.write("\n".getBytes());
            outputStream.write(fileName.getBytes());

            outputStream.write("\n".getBytes());
            outputStream.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

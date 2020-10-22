package per.demo;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InFileFileSystem { //extends FileSystem {

    private final InFileFileStore fileStore;

    InFileFileSystem(InFileFileStore fileStore) {
        this.fileStore = fileStore;
    }

    public void createFile(String name, String content) {
        try {
            fileStore.addContent(name, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateFile(String name, String newContent) {
        fileStore.delete(name);

        try {
            fileStore.addContent(name, newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String name) {
        fileStore.delete(name);
    }

    public String readFile(String name) {
        try {
            return fileStore.read(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> allFileNames() {
        return new ArrayList<>(fileStore.getMap().keySet());
    }

    public String getFileSystemName() {
        return fileStore.getName();
    }

    public ConcurrentHashMap<String, Pair<Long, Integer>> getMap() {
        return fileStore.getMap();
    }

    void destroy() {
        try {
            fileStore.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

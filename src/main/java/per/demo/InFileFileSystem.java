package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@RequiredArgsConstructor
public class InFileFileSystem { //extends FileSystem {

    @Getter
    private final String name;
    private final InFileFileStore fileStore;

    public void createFile(String name, String content) {
        try {
            fileStore.addContent(name, content);
        } catch (IOException e) {
            log.error("Failed to add content", e);
        }
    }

    public void updateFile(String name, String newContent) {
        fileStore.delete(name);

        try {
            fileStore.addContent(name, newContent);
        } catch (IOException e) {
            log.error("Failed to add content", e);
        }
    }

    public void deleteFile(String name) {
        fileStore.delete(name);
    }

    public String readFile(String name) {
        try {
            return fileStore.read(name);
        } catch (IOException e) {
            log.error("Failed to read content", e);
        }

        return "";
    }

    public List<String> allFileNames() {
        return new ArrayList<>(fileStore.getPositionsAndSizesByNames().keySet());
    }

    public ConcurrentMap<String, Triple<Long, Integer, Boolean>> getMap() {
        return fileStore.getPositionsAndSizesByNames();
    }

    void destroy() {
        try {
            fileStore.destroy();
        } catch (IOException e) {
            log.error("Failed to destroy", e);
        }
    }
}

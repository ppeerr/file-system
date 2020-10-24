package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
        try {
            fileStore.delete(name);
            fileStore.addContent(name, newContent);
        } catch (IOException e) {
            log.error("Failed to add content", e);
        }
    }

    public void deleteFile(String name) {
        try {
            fileStore.delete(name);
        } catch (IOException e) {
            log.error("Failed to add content", e);
        }
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
        return fileStore.getPositionsAndSizesByNames()
                .entrySet().stream()
                .filter(it -> it.getValue().isPresent())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public ConcurrentMap<String, MetaInfo> getMap() { //TODO immutable
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

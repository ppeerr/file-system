package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import per.demo.exception.*;

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
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    public void updateFile(String name, String newContent) {
        try {
            fileStore.delete(name);
            fileStore.addContent(name, newContent);
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }

    public void deleteFile(String name) {
        try {
            fileStore.delete(name);
        } catch (Exception e) {
            throw new DeleteFileException(e);
        }
    }

    public String readFile(String name) {
        try {
            return fileStore.read(name);
        } catch (Exception e) {
            throw new ReadFileException(e);
        }
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
        } catch (Exception e) {
            throw new DestroyFileSystemException(e);
        }
    }
}

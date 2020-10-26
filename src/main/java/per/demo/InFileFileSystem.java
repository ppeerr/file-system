package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import per.demo.exception.CreateFileException;
import per.demo.exception.DeleteFileException;
import per.demo.exception.DestroyFileSystemException;
import per.demo.exception.ReadFileException;
import per.demo.exception.UpdateFileException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class InFileFileSystem { //extends FileSystem {

    @Getter
    private final String name;

    private final InFileFileStore store;
    private final InFileFileStoreView storeView;

    public void createFile(String fileName, String content) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);
            if (meta != null && meta.isPresent()) {
                throw new RuntimeException("File '" + fileName + "' already exists");
            }

            List<FileInfo> metaInfosToUpdate = store.saveContent(fileName, content);

            storeView.putMeta(metaInfosToUpdate);
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    public void updateFile(String fileName, String newContent) {
        try {
            synchronized (storeView) {
                deleteFile(fileName);

                List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, newContent);

                storeView.putMeta(fileInfosToUpdate);
            }
        } catch (Exception e) {
            log.error("for content {}", newContent);
            throw new UpdateFileException(e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (meta == null || !meta.isPresent()) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            store.setDeletedMetaFlag(meta.getPresentPosition());

            storeView.remove(fileName);
        } catch (Exception e) {
            throw new DeleteFileException(e);
        }
    }

    public String readFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (meta == null || !meta.isPresent()) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            return store.readContent(meta.getStartPosition(), meta.getSize());
        } catch (Exception e) {
            throw new ReadFileException(fileName, e);
        }
    }

    public List<String> allFileNames() {
        return storeView.getMap()
                .entrySet().stream()
                .filter(it -> it.getValue().isPresent())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<String, MetaInfo> getMap() {
        return storeView.getMap();
    }

    public boolean contains(String fileName) {
        return storeView.contains(fileName);
    }

    void destroy() {
        try {
            store.destroy();
        } catch (Exception e) {
            throw new DestroyFileSystemException(e);
        }
    }
}

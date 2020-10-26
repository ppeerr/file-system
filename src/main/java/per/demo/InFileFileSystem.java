package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import per.demo.exception.*;

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
        MetaInfo meta = storeView.getMeta(fileName);
        if (meta != null && meta.isPresent()) {
            throw new CreateFileException("File '" + fileName + "' already exists");
        }

        List<FileInfo> metaInfosToUpdate;
        try {
            metaInfosToUpdate = store.saveContent(fileName, content);
        } catch (Exception e) {
            throw new CreateFileException(e);
        }

        storeView.putMeta(metaInfosToUpdate);
    }

    public void updateFile(String fileName, String newContent) {
        deleteFile(fileName);

        List<FileInfo> fileInfosToUpdate;
        try {
            fileInfosToUpdate = store.saveContent(fileName, newContent);
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }

        storeView.putMeta(fileInfosToUpdate);
    }

    public void deleteFile(String fileName) {
        MetaInfo meta = storeView.getMeta(fileName);

        if (meta == null || !meta.isPresent()) {
            throw new DeleteFileException("No file '" + fileName + "' found");
        }

        try {
            store.setDeletedMetaFlag(meta.getPresentPosition());
        } catch (Exception e) {
            throw new DeleteFileException(e);
        }

        storeView.remove(fileName);
    }

    public String readFile(String fileName) {
        MetaInfo meta = storeView.getMeta(fileName);

        if (meta == null || !meta.isPresent()) {
            throw new ReadFileException("No file '" + fileName + "' found");
        }

        try {
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

    void destroy() {
        try {
            store.destroy();
        } catch (Exception e) {
            throw new DestroyFileSystemException(e);
        }
    }
}

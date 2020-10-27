package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import per.demo.exception.*;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;
import per.demo.validator.UploadFileContentValidator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class InFileFileSystem {

    @Getter
    private final String name;

    private final InFileFileStore store;
    private final InFileFileStoreView storeView;

    public void createFile(String fileName, String content) {
        try {
            UploadFileContentValidator.check(fileName, content);

            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaExists(meta)) {
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
            UploadFileContentValidator.check(fileName, newContent);

            synchronized (storeView) {
                deleteFile(fileName);

                List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, newContent);

                storeView.putMeta(fileInfosToUpdate);
            }
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            store.setDeletedMetaFlag(meta.getPresentFlagPosition());

            storeView.remove(fileName);
        } catch (Exception e) {
            throw new DeleteFileException(e);
        }
    }

    public String readFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (isMetaDoesNotExist(meta)) {
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

    public boolean isOpen() {
        return store.isOpen();
    }

    public void close() {
        try {
            store.close();
        } catch (Exception e) {
            throw new DestroyFileSystemException(e);
        }
    }

    private boolean isMetaExists(MetaInfo meta) {
        return meta != null && meta.isPresent();
    }

    private boolean isMetaDoesNotExist(MetaInfo meta) {
        return meta == null || !meta.isPresent();
    }
}

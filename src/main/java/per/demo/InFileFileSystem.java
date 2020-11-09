package per.demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import per.demo.exception.*;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;
import per.demo.validator.UploadFileContentValidator;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main object for control the fileSystem from client.
 * The object is thread-safe and works with two other abstract parts:
 * <pre>
 * - {@linkplain InFileFileStore} -- implementation of data layer of system;
 * - {@linkplain InFileFileStoreView} -- in-memory view of stored files meta data. </pre>
 * <p>
 * There are five main public methods:
 * <pre>
 * - {@linkplain #createFile(String, String)} -- create a new file with content
 * - {@linkplain #updateFile(String, String)} -- update already created file with new content
 * - {@linkplain #deleteFile(String)} -- delete already existing file
 * - {@linkplain #readFile(String)} -- read the content of a specific file
 * - {@linkplain #close()} -- close closeable nio.FileChannel resource {@linkplain InFileFileStore#close()} </pre>
 * <p>
 * And four additional public methods:
 * <pre>
 * - {@linkplain #allFileNames()} -- get List of existing file names
 * - {@linkplain #getMap()} -- get Map model of Store view
 * - {@linkplain #contains(String)} -- check whether the file system contains file by specific name
 * - {@linkplain #isOpen()} -- check whether the file system is open (closable resource is still open) </pre>
 */
@RequiredArgsConstructor
public class InFileFileSystem implements Closeable {

    @Getter
    private final String name;

    private final InFileFileStore store;
    private final InFileFileStoreView storeView;

    /**
     * Create file with specific file name and content. If file with the same file name exists in store view
     * {@linkplain InFileFileStoreView#getMeta(String)}, then Exception is thrown.
     * A t first system save meta info and content into store, then update store view.
     *
     * @param fileName - not blank string, contains only latin letters and digits
     * @param content  - not blank, length not greater than {@linkplain UploadFileContentValidator#maxContentSizeBytes}
     */
    public void createFile(String fileName, String content) {
        try {
            UploadFileContentValidator.check(fileName, content);

            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaExists(meta)) {
                throw new RuntimeException("File '" + fileName + "' already exists");
            }

            save(fileName, content);
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    /**
     * Update file with specific file name replacing by new content. File with @param filename must be existed in system.
     * At first system delete file, then create new with new content.
     *
     * @param fileName   - not blank string, contains only latin letters and digits
     * @param newContent - not blank, length not greater than {@linkplain UploadFileContentValidator#maxContentSizeBytes}
     */
    public void updateFile(String fileName, String newContent) {
        try {
            UploadFileContentValidator.check(fileName, newContent);

            deleteFile(fileName);
            save(fileName, newContent);
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }

    /**
     * Delete file by specific file name. File with @param filename must be existed in system.
     * At first system delete file in store, then delete from store view.
     *
     * @param fileName - name of existed file
     */
    public void deleteFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            remove(fileName, meta);
        } catch (Exception e) {
            throw new DeleteFileException(e);
        }
    }

    /**
     * Read file content by specific file name. File with @param filename must be existed in system.
     *
     * @param fileName - name of existed file
     * @return content of specific file
     */
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

    public File getFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);

            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            return new File(store, meta);
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

    /**
     * Close internal closable resource store {@linkplain InFileFileStore}.
     * After that we can't read or store content using this instance of file system, so we should create a new instance
     * for it using {@linkplain FileSystemFactory} methods.
     */
    public void close() {
        try {
            store.close();
        } catch (Exception e) {
            throw new DestroyFileSystemException(e); //TODO check
        }
    }

    private void save(String fileName, String content) {
        synchronized (storeView) {
            List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, content);

            storeView.putMeta(fileInfosToUpdate);
        }
    }

    private void remove(String fileName, MetaInfo meta) {
        synchronized (storeView) {
            store.setDeletedMetaFlag(meta.getPresentFlagPosition());

            storeView.remove(fileName);
        }
    }

    private boolean isMetaExists(MetaInfo meta) {
        return meta != null && meta.isPresent();
    }

    private boolean isMetaDoesNotExist(MetaInfo meta) {
        return meta == null || !meta.isPresent();
    }
}

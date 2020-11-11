package per.demo.extendable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import per.demo.*;
import per.demo.exception.*;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;
import per.demo.validator.UploadFileContentValidator;

import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
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
 * - {@linkplain #readFileToString(String)} -- read the content of a specific file
 * - {@linkplain #close()} -- close closeable resource {@linkplain InFileFileStore#close()} </pre>
 * <p>
 * And four additional public methods:
 * <pre>
 * - {@linkplain #allFileNames()} -- get List of existing file names
 * - {@linkplain #getMap()} -- get Map model of Store view
 * - {@linkplain #contains(String)} -- check whether the file system contains file by specific name
 * - {@linkplain #isOpen()} -- check whether the file system is open (closable resource is still open) </pre>
 */
@RequiredArgsConstructor
public class InFileFileSystemImpl implements InFileFileSystem {

    @Getter
    private final String name;

    private final InFileFileStore store;
    private final InFileFileStoreView storeView;

    @Override
    public void createFile(String fileName, String content) {
        try {
            UploadFileContentValidator.check(fileName, content);

            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaExists(meta)) {
                throw new RuntimeException("File '" + fileName + "' already exists");
            }

            synchronized (storeView) {
                save(fileName, content);
            }
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }
    @Override
    public void createFile(String fileName, InputStream contentStream) {
        try {
            UploadFileContentValidator.checkFileName(fileName);

            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaExists(meta)) {
                throw new RuntimeException("File '" + fileName + "' already exists");
            }

            synchronized (storeView) {
                save(fileName, contentStream);
            }
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }
    @Override
    public void createFile(String fileName, ReadableByteChannel contentChannel) {
        try {
            UploadFileContentValidator.checkFileName(fileName);

            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaExists(meta)) {
                throw new RuntimeException("File '" + fileName + "' already exists");
            }

            synchronized (storeView) {
                save(fileName, contentChannel);
            }
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    @Override
    public void updateFile(String fileName, String newContent) {
        try {
            UploadFileContentValidator.check(fileName, newContent);

            synchronized (storeView) {
                MetaInfo meta = storeView.getMeta(fileName);
                if (isMetaDoesNotExist(meta)) {
                    throw new RuntimeException("No file '" + fileName + "' found");
                }

                remove(fileName, meta);
                save(fileName, newContent);
            }
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }
    @Override
    public void updateFile(String fileName, InputStream contentStream) {
        try {
            UploadFileContentValidator.checkFileName(fileName);

            synchronized (storeView) {
                MetaInfo meta = storeView.getMeta(fileName);
                if (isMetaDoesNotExist(meta)) {
                    throw new RuntimeException("No file '" + fileName + "' found");
                }

                remove(fileName, meta);
                save(fileName, contentStream);
            }
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }
    public void updateFile(String fileName, ReadableByteChannel contentChannel) {
        try {
            UploadFileContentValidator.checkFileName(fileName);

            synchronized (storeView) {
                MetaInfo meta = storeView.getMeta(fileName);
                if (isMetaDoesNotExist(meta)) {
                    throw new RuntimeException("No file '" + fileName + "' found");
                }

                remove(fileName, meta);
                save(fileName, contentChannel);
            }
        } catch (Exception e) {
            throw new UpdateFileException(e);
        }
    }

    /**
     * Delete file by specific file name. File with @param filename must be existed in system.
     * At first system delete file in store, then delete from store view.
     *
     * @param fileName name of existed file
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            synchronized (storeView) {
                remove(fileName, meta);
            }
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
    @Override
    public String readFileToString(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            return store.readContentString(meta.getStartPosition(), meta.getSize());
        } catch (Exception e) {
            throw new ReadFileException(fileName, e);
        }
    }

    @Override
    public File getFile(String fileName) {
        try {
            MetaInfo meta = storeView.getMeta(fileName);
            if (isMetaDoesNotExist(meta)) {
                throw new RuntimeException("No file '" + fileName + "' found");
            }

            return new FileImpl(fileName, store, meta);
        } catch (Exception e) {
            throw new ReadFileException(fileName, e);
        }
    }

    @Override
    public List<String> allFileNames() {
        return storeView.getMap()
                .entrySet().stream()
                .filter(it -> it.getValue().isPresent())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, MetaInfo> getMap() {
        return storeView.getMap();
    }

    @Override
    public boolean contains(String fileName) {
        return storeView.contains(fileName);
    }

    @Override
    public boolean isOpen() {
        return store.isOpen();
    }

    /**
     * Close internal closable resource store {@linkplain InFileFileStore}.
     * After that we can't read or store content using this instance of file system, so we should create a new instance
     * for it using {@linkplain FileSystemFactory} methods.
     */
    @Override
    public void close() {
        try {
            store.close();
        } catch (Exception e) {
            throw new DestroyFileSystemException(e);
        }
    }

    private void save(String fileName, String content) {
        List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, content);

        storeView.putMeta(fileInfosToUpdate);
    }

    private void save(String fileName, InputStream contentStream) {
        List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, contentStream);

        storeView.putMeta(fileInfosToUpdate);
    }

    private void save(String fileName, ReadableByteChannel contentChannel) {
        List<FileInfo> fileInfosToUpdate = store.saveContent(fileName, contentChannel);

        storeView.putMeta(fileInfosToUpdate);
    }

    private void remove(String fileName, MetaInfo meta) {
        store.delete(meta.getPresentFlagPosition());

        storeView.remove(fileName);
    }

    private boolean isMetaExists(MetaInfo meta) {
        return meta != null && meta.isPresent();
    }

    private boolean isMetaDoesNotExist(MetaInfo meta) {
        return meta == null || !meta.isPresent();
    }
}

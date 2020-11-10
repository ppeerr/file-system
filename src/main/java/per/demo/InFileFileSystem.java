package per.demo;

import per.demo.model.MetaInfo;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public interface InFileFileSystem extends Closeable {

    /**
     * Create file with specific file name and content.
     * If file with the same file name exists in store view then Exception is thrown.
     *
     * @param fileName name of new file
     * @param content string content
     */
    void createFile(String fileName, String content);

    /**
     * Update file with specific file name.
     * File with @param filename must be existed in system.     *
     *
     * @param fileName name of new file
     * @param newContent string content
     */
    void updateFile(String fileName, String newContent);

    /**
     * Delete file by specific file name.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of file
     */
    void deleteFile(String fileName);

    /**
     * Read file content to string by specific file name.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of file
     * @return string content of specific file
     */
    String readFileToString(String fileName);

    /**
     * Get File abstraction representing content in file system.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of file
     * @return {@linkplain  per.demo.File} abstraction
     */
    File getFile(String fileName);

    List<String> allFileNames();

    Map<String, MetaInfo> getMap();

    boolean contains(String fileName);

    boolean isOpen();

    String getName();
}

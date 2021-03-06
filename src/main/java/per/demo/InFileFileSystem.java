package per.demo;

import per.demo.model.MetaInfo;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
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
     * Create file with specific file name and content.
     * If file with the same file name exists in store view then Exception is thrown.
     *
     * @param fileName name of new file
     * @param contentStream InputStream content
     */
    void createFile(String fileName, InputStream contentStream);

    /**
     * Create file with specific file name and content.
     * If file with the same file name exists in store view then Exception is thrown.
     *
     * @param fileName name of new file
     * @param contentChannel ReadableByteChannel content
     */
    void createFile(String fileName, ReadableByteChannel contentChannel);

    /**
     * Update file with specific file name.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of new file
     * @param newContent string content
     */
    void updateFile(String fileName, String newContent);

    /**
     * Update file with specific file name.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of new file
     * @param contentStream content byte stream
     */
    void updateFile(String fileName, InputStream contentStream);

    /**
     * Update file with specific file name.
     * File with @param filename must be existed in system.
     *
     * @param fileName name of new file
     * @param contentChannel ReadableByteChannel stream
     */
    void updateFile(String fileName, ReadableByteChannel contentChannel);

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
     * @return {@linkplain  File} abstraction
     */
    File getFile(String fileName);

    List<String> allFileNames();

    /**
     * Get meta info map
     *
     * @return Map with meta info by file name
     */
    Map<String, MetaInfo> getMap();

    boolean contains(String fileName);

    boolean isOpen();

    String getName();
}

package per.demo;

import per.demo.model.FileInfo;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

public interface InFileFileStore extends Closeable {

    List<FileInfo> saveContent(String fileName, String content);

    List<FileInfo> saveContent(String fileName, InputStream contentStream);

    List<FileInfo> saveContent(String fileName, ReadableByteChannel contentChannel);

    String readContentString(long pos, long size);

    byte[] readContentBytes(long pos, long size);

    void delete(long presentFlagPosition);

    boolean isOpen();

    String getFilePath();
}

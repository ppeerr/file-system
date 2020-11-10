package per.demo;

import per.demo.model.FileInfo;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.util.List;

public interface InFileFileStore extends Closeable {

    List<FileInfo> saveContent(String fileName, String content);

    List<FileInfo> saveContent(String fileName, ByteArrayInputStream contentStream);

    String readContent(long pos, long size);

    byte[] readContentBytes(long pos, long size);

    void delete(long presentFlagPosition);

    boolean isOpen();
}

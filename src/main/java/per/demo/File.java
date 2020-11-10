package per.demo;

public interface File {

    byte[] read(long byteCount);

    byte[] read(long offset, long byteCount);

    long getSize();
}

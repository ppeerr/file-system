package per.demo;

public interface File {

    String getName();

    byte[] read(long byteCount);

    byte[] read(long offset, long byteCount);

    long getSize();
}

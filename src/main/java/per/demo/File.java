package per.demo;

public interface File {

    String getName();

    byte[] getBytes();

    byte[] read(long byteCount);

    byte[] read(long offset, long byteCount);

    long getSize();
}

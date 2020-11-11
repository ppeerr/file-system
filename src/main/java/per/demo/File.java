package per.demo;

public interface File {

    String getName();

    /**
     * Read file all bytes.
     *
     * @return result byte array with whole file
     */
    byte[] getBytes();

    /**
     * Read specific bytes count from the beginning.
     * If @param byteCount greater than file size, then just byte array with whole file will be returned.
     *
     * @param byteCount non-negative byte count
     * @return result byte array
     */
    byte[] read(long byteCount);

    /**
     * Read specific bytes count from the beginning with given offset.
     * If requesting part length greater than byte length from @param offset to file end,
     * then just part from @param offset to file end will be returned.
     *
     * @param byteCount non-negative byte count
     * @param offset offset from the beginning of file. Must be smaller than file size
     * @return result byte array
     */
    byte[] read(long offset, long byteCount);

    long getSize();
}

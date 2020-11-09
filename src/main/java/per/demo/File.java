package per.demo;

import org.apache.commons.lang3.Validate;
import per.demo.model.MetaInfo;

public class File {
    private final InFileFileStore store;
    private final MetaInfo metaInfo;

    File(InFileFileStore store, MetaInfo metaInfo) {
        Validate.isTrue(store.isOpen(), "store must be open");

        this.store = store;
        this.metaInfo = metaInfo;
    }

    public byte[] read(long byteCount) {
        return read(0L, byteCount);
    }

    public byte[] read(long offset, long byteCount) {
        Validate.isTrue(offset < metaInfo.getSize(), "offset must be less than file size");
        long neededByteCount = Math.min(byteCount, metaInfo.getSize() - offset);

        return store.readContentBytes(metaInfo.getStartPosition() + offset, neededByteCount);
    }
}

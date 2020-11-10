package per.demo;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import per.demo.model.MetaInfo;

public class File {
    @Getter
    private final String name;
    private final InFileFileStore store;
    private final MetaInfo metaInfo;

    File(String name, InFileFileStore store, MetaInfo metaInfo) {
        Validate.isTrue(StringUtils.isNotEmpty(name), "name must not be empty");
        Validate.isTrue(store.isOpen(), "store must be open");
        Validate.isTrue(metaInfo != null, "metaInfo must not be null");

        this.name = name;
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

    public long getSize() {
        return metaInfo.getSize();
    }
}

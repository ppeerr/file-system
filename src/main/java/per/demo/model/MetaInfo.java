package per.demo.model;

import lombok.Getter;

@Getter
public class MetaInfo {
    private static final int UNDEFINED_PRESENT_FLAG_POSITION = -1;

    private final long startPosition;
    private final long size;
    private final long presentFlagPosition;

    private final boolean present;

    public MetaInfo(long startPosition, long size, long presentFlagPosition) {
        this.startPosition = startPosition;
        this.size = size;
        this.presentFlagPosition = presentFlagPosition;
        this.present = true;
    }

    public MetaInfo(long startPosition, long size) {
        this.startPosition = startPosition;
        this.size = size;
        this.presentFlagPosition = UNDEFINED_PRESENT_FLAG_POSITION;
        this.present = true;
    }

    public MetaInfo(MetaInfo from) {
        this.startPosition = from.getStartPosition();
        this.size = from.getSize();
        this.presentFlagPosition = from.getPresentFlagPosition();
        this.present = from.isPresent();
    }
}

package per.demo.model;

import lombok.Getter;

@Getter
public class MetaInfo {
    private final long startPosition;
    private final int size;
    private final long presentFlagPosition;

    private final boolean present;

    public MetaInfo(long startPosition, int size, long presentFlagPosition) {
        this.startPosition = startPosition;
        this.size = size;
        this.presentFlagPosition = presentFlagPosition;
        this.present = true;
    }

    public MetaInfo(MetaInfo from) {
        this.startPosition = from.getStartPosition();
        this.size = from.getSize();
        this.presentFlagPosition = from.getPresentFlagPosition();
        this.present = from.isPresent();
    }
}

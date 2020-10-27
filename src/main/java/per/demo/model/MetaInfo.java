package per.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MetaInfo {
    private final long startPosition;
    private final int size;
    private final long presentFlagPosition;

    private boolean present = true;

    public MetaInfo(MetaInfo from) {
        this.startPosition = from.getStartPosition();
        this.size = from.getSize();
        this.presentFlagPosition = from.getPresentFlagPosition();
        this.present = from.isPresent();
    }
}

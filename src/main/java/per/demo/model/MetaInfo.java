package per.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MetaInfo {
    private final long startPosition;
    private final int size;
    private final long presentPosition;

    private boolean present = true;

    public MetaInfo(MetaInfo from) {
        this.startPosition = from.getStartPosition();
        this.size = from.getSize();
        this.presentPosition = from.getPresentPosition();
        this.present = from.isPresent();
    }
}

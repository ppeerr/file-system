package per.demo;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class MetaInfo {
    private final long startPosition;
    private final int size;
    private final long presentPosition;

    private boolean present = true;
}

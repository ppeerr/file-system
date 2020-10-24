package per.demo;

import lombok.Data;

@Data
public class MetaInfo {
    private final long startPosition;
    private final int size;
    private final long isPresentPosition;

    private boolean present = true;
}

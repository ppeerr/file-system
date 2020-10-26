package per.demo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@RequiredArgsConstructor
public class FileInfo {
    private final String name;
    private final MetaInfo metaInfo;

    private boolean present = true;

    public FileInfo(FileInfo from) {
        this.name = from.getName();
        this.metaInfo = new MetaInfo(from.getMetaInfo());
    }
}

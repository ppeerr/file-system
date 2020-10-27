package per.demo.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FileInfo {
    private final String name;
    private final MetaInfo metaInfo;

    public FileInfo(FileInfo from) {
        this.name = from.getName();
        this.metaInfo = new MetaInfo(from.getMetaInfo());
    }
}

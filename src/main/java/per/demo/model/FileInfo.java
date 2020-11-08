package per.demo.model;

import lombok.Getter;

@Getter
public class FileInfo {
    private final String name;
    private final MetaInfo metaInfo;

    public FileInfo(String name, MetaInfo metaInfo) {
        this.name = name;
        this.metaInfo = metaInfo;
    }
}

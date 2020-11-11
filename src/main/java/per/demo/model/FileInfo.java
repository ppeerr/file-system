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

    public static FileInfo from(String stringElement) {
        String[] metaElementParts = stringElement.split(",");

        String fileName = metaElementParts[0].substring(2, metaElementParts[0].length() - 1);
        long start = Long.parseLong(metaElementParts[1]);
        long size = Integer.parseInt(metaElementParts[2]);

        return new FileInfo(fileName, new MetaInfo(start, size));
    }
}

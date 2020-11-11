package per.demo;

import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;

import java.util.List;
import java.util.Map;

public interface InFileFileStoreView {

    boolean contains(String fileName);

    MetaInfo getMeta(String fileName);

    void putMeta(List<FileInfo> fileInfos);

    void remove(String fileName);

    /**
     * Get meta info map
     *
     * @return Map with meta info by file name
     */
    Map<String, MetaInfo> getMap();
}

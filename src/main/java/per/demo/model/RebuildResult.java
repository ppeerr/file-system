package per.demo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@RequiredArgsConstructor
public class RebuildResult {
    private final List<FileInfo> fileInfosToUpdate;
    private final Path file;
    private final FileChannel channel;
    private final AtomicLong metaPos;
    private final int metaBytesCount;
}

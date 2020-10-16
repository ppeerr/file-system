package per.demo;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public class InFileFileSystem extends FileSystem {
    private final InFileFileSystemProvider provider;
    private final URI uri;

    private final InFileFileStore fileStore;
    private final PathService pathService;

    private final UserPrincipalLookupService userLookupService = new UserLookupService();

    private final FileSystemView defaultView;

    public InFileFileSystem(InFileFileSystemProvider provider, URI uri, InFileFileStore fileStore, PathService pathService, FileSystemView defaultView) {
        this.provider = provider;
        this.uri = uri;
        this.fileStore = fileStore;
        this.pathService = pathService;
        this.defaultView = defaultView;
    }

    @Override
    public FileSystemProvider provider() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return null;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    @Override
    public Path getPath(String s, String... strings) {
        return null;
    }

    @Override
    public PathMatcher getPathMatcher(String s) {
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }
}

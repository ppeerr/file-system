package per.demo;

public class InFileFileSystem { //extends FileSystem {
//    private final InFileFileSystemProvider provider;
//    private final URI uri;

    private final InFileFileStore fileStore;
//    private final PathService pathService;

//    private final UserPrincipalLookupService userLookupService = new UserLookupService();

//    private final FileSystemView defaultView;

    public InFileFileSystem(InFileFileStore fileStore) {
        this.fileStore = fileStore;
    }

    public void createFile(String name, String content) {
        fileStore.add(name);
        fileStore.addContent(name, content);
    }

    public void updateFile(String name, String newContent) {

    }

    public void deleteFile(String name) {

    }

    public String readFile(String name) {
        return "";
    }

    private void findFile(String name) {

    }
}

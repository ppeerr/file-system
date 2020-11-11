package per.demo.extendable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;
import per.demo.model.RebuildResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static per.demo.utils.MetaInfoUtils.*;

class InFileFileStoreRebuildService {

    private static final String REBUILD_BUF_FILE_SUFFIX = ".buf";

    private final String metaHeader;
    private final String metaDelimiter;

    private Path file;
    private FileChannel channel;

    private AtomicLong metaPos;
    private int metaBytesCount;

    InFileFileStoreRebuildService(String metaHeader, String metaDelimiter, Path file, FileChannel channel) {
        this.metaHeader = metaHeader;
        this.metaDelimiter = metaDelimiter;
        this.file = file;
        this.channel = channel;
    }

    @SneakyThrows
    RebuildResult increaseMetaSpaceAndRebuild(int metaBytesCount, String oldMeta, FileInfo addingFileInfo) {
        int newMetaBytesCount = metaBytesCount << 1;
        int newMetaPos = (metaHeader + "\n").getBytes(StandardCharsets.UTF_8).length;

        //create buffer channel
        Path bufFile = Files.createFile(Paths.get(file.toString() + REBUILD_BUF_FILE_SUFFIX));
        FileChannel bufChannel = getFileChannel(bufFile);

        writeInitialMetaState(newMetaBytesCount, bufChannel);

        //get Files info for all old saved files including the new adding File
        List<FileInfo> oldFileInfos = getOldFileInfos(oldMeta);
        oldFileInfos.add(addingFileInfo);

        FilesAndMetaInfoHolder fileAndMetaInfo = rebuild(newMetaPos, oldFileInfos, bufChannel);

        //write new meta content to buffer channel
        String metaContentToStore = String.join("", fileAndMetaInfo.getMetaInfosToStore());
        ByteBuffer buff = ByteBuffer.wrap((metaContentToStore).getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff, newMetaPos);
        bufChannel.force(false);

        renewFromBufChannel(newMetaBytesCount, newMetaPos, bufFile, bufChannel, metaContentToStore);

        return new RebuildResult(
                fileAndMetaInfo.getFileInfosToUpdate(),
                this.file,
                this.channel,
                this.metaPos,
                this.metaBytesCount
        );
    }

    private void writeInitialMetaState(int newMetaBytesCount, FileChannel bufChannel) throws IOException {
        String newInitialStartState = getInitialMetaContent(metaHeader, newMetaBytesCount, metaDelimiter);

        ByteBuffer buff = ByteBuffer.wrap(newInitialStartState.getBytes(StandardCharsets.UTF_8));
        bufChannel.write(buff);
    }

    private List<FileInfo> getOldFileInfos(String oldMeta) {
        return getMetaDataElements(oldMeta).stream()
                .filter(withPresentState())
                .map(FileInfo::from)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private FilesAndMetaInfoHolder rebuild(int presentFlagPos, List<FileInfo> oldFileInfos, FileChannel bufChannel) {
        List<String> metaInfosToStore = new ArrayList<>();
        List<FileInfo> fileInfosToUpdate = new ArrayList<>();

        for (int i = 0; i < oldFileInfos.size(); i++) {
            FileInfo metaElement = oldFileInfos.get(i);
            MetaInfo metaInfo = metaElement.getMetaInfo();

            long newStart = moveContentToBufferChannel(bufChannel, metaInfo);

            String currentNewFileInfoElement = getMetaElement(metaElement, metaInfo, newStart);
            if (notLastFileInfo(i, oldFileInfos)) {
                metaInfosToStore.add(currentNewFileInfoElement);
            }

            //calc new meta info for current file
            presentFlagPos += currentNewFileInfoElement
                    .substring(0, currentNewFileInfoElement.length() - 2).getBytes(StandardCharsets.UTF_8).length;

            fileInfosToUpdate.add(new FileInfo(
                    metaElement.getName(),
                    new MetaInfo(newStart, metaInfo.getSize(), presentFlagPos)
            ));

            presentFlagPos += currentNewFileInfoElement
                    .substring(currentNewFileInfoElement.length() - 2).getBytes(StandardCharsets.UTF_8).length;
            //
        }

        return new FilesAndMetaInfoHolder(metaInfosToStore, fileInfosToUpdate);
    }

    private void renewFromBufChannel(
            int newMetaBytesCount,
            int newMetaPos,
            Path bufFile,
            FileChannel bufChannel,
            String metaContentToStore
    ) throws IOException {
        this.channel.close();
        long newEndPos = bufChannel.size();
        bufChannel.close();

        Files.move(bufFile, Paths.get(this.file.toString()), REPLACE_EXISTING);

        this.file = Paths.get(this.file.toString());
        this.channel = getFileChannel(this.file).position(newEndPos);

        metaPos = new AtomicLong(newMetaPos + metaContentToStore.length());
        metaBytesCount = newMetaBytesCount;
    }

    @SneakyThrows
    private FileChannel getFileChannel(Path file) {
        return FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
    }

    private long moveContentToBufferChannel(FileChannel bufChannel, MetaInfo metaInfo) throws IOException {
        long newStart = bufChannel.position();
        channel.transferTo(metaInfo.getStartPosition(), metaInfo.getSize(), bufChannel);

        return newStart;
    }

    private String getMetaElement(FileInfo metaElement, MetaInfo metaInfo, long newStart) {
        return "{\"" + metaElement.getName() + "\"," + newStart + "," + metaInfo.getSize() + "," + "A}";
    }

    private boolean notLastFileInfo(int i, List<FileInfo> oldFileInfos) {
        return i != oldFileInfos.size() - 1;
    }

    @Getter
    @RequiredArgsConstructor
    private static class FilesAndMetaInfoHolder {
        private final List<String> metaInfosToStore;
        private final List<FileInfo> fileInfosToUpdate;
    }
}

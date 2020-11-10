InFileFileSystem
=====
#Intro
Файловая систему на базе одного файла (все данные хранятся внутри одного файла). 
Есть поддержка для операций создания, удаления файлов, а также чтения, записи контента. 
Документированный API для работы с файловой системой.

27-10-2020 / version 0.1.0:
версия MVP -- реализован минимум операций, не самым оптимальным способом.
done:
- concurrency tests (++++)
- meta in the beginning of file (++++)
- store size and position in file (++++)
- sync in-memory collections with file content (++++)
- check and refactor closeable resources (++++)
- check and refactor synchronized parts (++++)
- add ability of creating system from existing file (++++)
- add logger (++++)
- add lombok (++++)
- set JDK 11 up (++++)
- add extend meta space algo (++++)
- refactor structure for 'Prod' quality (++++)
- improve delete by working with meta space (++++)
- add some tests and refactor them (++++)
- check is it 'Prod' quality? :) (++++)

В следующей версии планируется работа по:
- check and may be add File-area locks;
- delete (replace by spaces) file content in file;
- check and may be refactor buffers;
- add static-sized store impl. Looks like such impl will be faster, but without dynamic store size;
- improve javaDoc;
- refactor quite complex InFileFileStore class;

Текущие Trade-off'ы и допущения:
- для легкости разработки тестирования примем реализацию на строках вместо чистых байтов. Очевидно, 
    что манипуляции будут работать и для массивов байт.
- имена файлов внутри реализованной файловой системы могут содержать только латинские буквы и арабские цифры. 
    Это нужно для облегчения парсинга.

Getting started
---------------
The latest release is [0.1.0](https://github.com/ppeerr/file-system/releases/tag/v0.1.0)

Basic use
---------
Самый простой путь для использования `InFileFileSystem`, это просто создать новый экземпляр, используя `FileSystemFactory`
и начать использовать его:

```java
import per.demo.FileSystemFactory;
...

// For a simple file system with default config:
InFileFileSystem fs = FileSystemFactory.newFileSystem();
fs.createFile("test", "content");
```

What's supported?
-----------------

На данный момент InFileFileSystem поддерживает:

- Создание, удаление, обновление и чтение файлов.
- Чтение и запись происходит с помощью Closable ресурса `java.nio.channels.FileChannel`.
- Файловая система может быть создана из существующего файла,
    при условии, что файл верного формата (см. per.demo.validator.ExistingFileValidator).
- Файловая система является thread-safe реализацией.
- Реализована модель саморасширяющегося хранилища файлов (см. per.demo.InFileFileStoreImpl).
- Для кеширования мета информации о файлах используется реализация Store view (см. per.demo.InFileFileStoreView)
- Внутри оного jvm приложения не должно существовать два экземпляра `InFileFileSystem`, привязанного к одному файлу в ОС;
    за этим следит фабрика (см. per.demo.FileSystemFactory)
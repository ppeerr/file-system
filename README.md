TODO:
- parallel tests (++--)
- tree in the beginning of file (++--)
- store size and position in file (++++)
- delete file content in file
- sync in-memory collections with file content (++--)
- check and refactor closeable resources (++--)
- check and refactor synchronized parts
- add ability of creating system from existent file (++++)
- refactor to Byte storing from String storing
- refactor buffers
- add logger (++++)
- implement nio.FileSystem interfaces 
- add lombok (++++)
- add File lock
- set JDK 11 up (++++)

Trade-off'ы и допущения:
- для легкости разработки тестирования примем реализацию на строках вместо чистых байтов. Очевидно, 
    что манипуляции будут работать и для массивов байт.
- имена файлов внутри реализованной файловой системы не могут содержать '{' или '}'. Это нужно для облегчения парсинга.
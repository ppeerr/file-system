TODO:
- concurrency tests (++--)
- meta in the beginning of file (++++)
- store size and position in file (++++)
- delete file content in file
- sync in-memory collections with file content (++++)
- check and refactor closeable resources (++--)
- check and refactor synchronized parts
- add ability of creating system from existent file (++++)
- refactor buffers
- add logger (++++)
- add lombok (++++)
- add File lock
- set JDK 11 up (++++)
- add extend meta space algo (++++)
- add static-sized store impl
- refactor structure for 'Prod' quality
- improve delete by working with meta space
- add some tests and refactor them (+---)

- check is it 'Prod' quality? :)

NOT in MVP:
- refactor to Byte storing from String storing (----)
- implement nio.FileSystem interfaces (----)

Trade-off'ы и допущения:
- для легкости разработки тестирования примем реализацию на строках вместо чистых байтов. Очевидно, 
    что манипуляции будут работать и для массивов байт.
- имена файлов внутри реализованной файловой системы не могут содержать '{' или '}'. Это нужно для облегчения парсинга.
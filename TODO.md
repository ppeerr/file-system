1. (**DONE**) У FileSystemFactory есть статический метод close 
    https://github.com/ppeerr/file-system/blob/main/src/main/java/per/demo/FileSystemFactory.java#L99. 
    При этом у самого InFileFileSystem есть метод публичный close, но он не реализует Closable. 
    Не понятно, что правильно использовать.

2. (**DONE**) Невозможно иметь несколько независимых FileSystemFactory

3. (**DONE**) У метода FileSystemFactory#close нет никакой синхронизации на UPDATE_INSTANCES_LOCK, 
    кажется у него с методом newFileSystem может быть гонка данных.

4. (**DONE**) Невозможно записать/считать файл большого размера (например, 10гб)

5. (**DONE**) https://github.com/ppeerr/file-system/blob/main/src/main/java/per/demo/InFileFileSystem.java#L81 
    тут делается synchronized на storeView, потом делаются операции типа deleteFile. 
    При этом deleteFile публичный, и его можно вызвать вообще без синхронизации извне

6. (**DONE**) Из-за отсутствия интерфейсов и большой генерации кода на Lombok тяжело понять модель, приходится много вчитываться

7. (**DONE**) Смена кодировки между перезапускали приводит к некорректной работе

8. https://github.com/ppeerr/file-system/blob/main/src/main/java/per/demo/InFileFileStore.java#L165 Страшный метод с кучей магии и модификацией объекта, его не понять за разумное время
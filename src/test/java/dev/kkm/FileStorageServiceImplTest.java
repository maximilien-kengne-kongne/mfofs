package dev.kkm;

import dev.kkm.service.FileStorageServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceImplTest {

    private FileStorageServiceImpl service;
    private final String baseDir = "test-uploads";

    @BeforeEach
    void setup()  {
        service = new FileStorageServiceImpl();
        TestUtils.setField(service, "baseDir", baseDir);
        service.init();
    }

    @AfterEach
    void cleanup() throws IOException {
        Path basePath = Paths.get(baseDir);
        if (Files.exists(basePath)) {
            Files.walk(basePath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        }
    }

    @Test
    void testCreateDirectory() {
        String dirPath = "dir1";
        String createdPath = service.createDirectory(dirPath);
        assertTrue(Files.exists(Paths.get(baseDir, dirPath)));
        assertEquals(dirPath, createdPath);
    }

    @Test
    void testAddFile()  {
        String targetDir = "docs";
        String fileName = "note.txt";
        MockMultipartFile mockFile = new MockMultipartFile("file", fileName, "text/plain", "Hello World".getBytes());

        String relativePath = service.addFile(mockFile, targetDir, fileName);

        Path expectedPath = Paths.get(baseDir, targetDir, fileName);
        assertTrue(Files.exists(expectedPath));
        assertEquals(Paths.get(targetDir, fileName).toString(), relativePath);
    }

    @Test
    void testGetFileAsResource() throws IOException {
        String dir = "downloads";
        String name = "hello.txt";
        Files.createDirectories(Paths.get(baseDir, dir));
        Files.write(Paths.get(baseDir, dir, name), "hello".getBytes());

        Resource resource = service.getFileAsResource(Paths.get(dir, name).toString());

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testRenameDirectory() {
        String oldDir = "old";
        String newDir = "new";
        service.createDirectory(oldDir);

        Path path = Paths.get(baseDir, oldDir);
        assertTrue(Files.exists(path));

        service.renameDirectory(oldDir, newDir);

        assertTrue(Files.exists(Paths.get(baseDir, newDir)));
        assertFalse(Files.exists(path));
    }


    @Test
    void testDeleteFile() throws IOException {
        String dir = "temp";
        String file = "todelete.txt";
        service.createDirectory(dir);
        Path filePath = Paths.get(baseDir, dir, file);
        Files.write(filePath, "Delete me".getBytes());

        service.delete(Paths.get(dir, file).toString());

        assertFalse(Files.exists(filePath));
    }

    @Test
    void testExistsAndTypeCheck() throws IOException {
        String file = "check.txt";
        Files.write(Paths.get(baseDir, file), "Check content".getBytes());

        assertTrue(service.exists(file));
        assertTrue(service.isFile(file));
        assertFalse(service.isDirectory(file));
    }

    @Test
    void testListFilesAndDirectories() throws IOException {
        String dir = "mixed";
        Path dirPath = Paths.get(baseDir, dir);
        Files.createDirectories(dirPath);
        Files.createFile(dirPath.resolve("file1.txt"));
        Files.createDirectories(dirPath.resolve("subdir"));

        List<String> files = service.listFiles(dir);
        List<String> dirs = service.listDirectories(dir);

        assertTrue(files.contains("file1.txt"));
        assertTrue(dirs.contains("subdir"));
    }
}


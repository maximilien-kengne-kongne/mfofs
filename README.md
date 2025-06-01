# 📁 File Storage Service

A modular and reusable file storage service in Java 17 with Spring Boot. It supports file and directory management operations (add, copy, move, delete, rename, list, read...) directly on the filesystem.

---

## ✨ Key Features

- ✅ Add files to a given path
- ✅ Create directories (recursive if needed)
- ✅ Read files as org.springframework.core.io.Resource
- ✅ List files or folders
- ✅ Copy / Move files or folders
- ✅ Rename files or folders
- ✅ Delete files or folders
- ✅ Path validation with automatic sanitization
- ✅ Logging of operations


---

## Technologies

- Java 17+
- Spring Boot : 3.4.4
- Maven
- Spring Core (Resource)
- SLF4J / Logback


## Getting Started

To use this starter you will need to add the following dependency to your project.

```xml
<dependency>
    <groupId>io.github.maximilien-kengne-kongne</groupId>
    <artifactId>mfofs-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Note
Don't add again these dependencies in your project

``` xml
	<dependencies>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-configuration-processor</artifactId>
			<optional>true</optional>
		</dependency>

	</dependencies>
```

---
## Configuration

Add these properties to your `application.properties or application.yml`:

``` properties
file.storage.base-dir=uploads
```


#### Inject this bean in your service class

``` java
private FileStorageService fileStorageService;
```

## How to use ?

### 📥 1. Add a file

``` java
MultipartFile file = ...; // from HTTP request
String targetPath = "docs/manuals";
String newName = "guide.pdf";
String fullPath = fileStorageService.addFile(file, targetPath, newName);
System.out.println("File added at: " + fullPath);
```

### 📁 2. Create a directory

``` java
String directoryPath = "archive/2025/documents";
fileStorageService.createDirectory(directoryPath);
```

### 📤 3. Read a file as Resource

``` java
String filePath = "docs/manuals/guide.pdf";
Resource resource = fileStorageService.getFileAsResource(filePath);

String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
System.out.println(content);
    
```
### 🗃️ 4. List files in a directory

``` java

  List<String> fileNames = fileStorageService.listFiles("docs/manuals");
fileNames.forEach(System.out::println);

```

### 📂 5. List subdirectories

``` java
  List<String> folderNames = fileStorageService.listDirectories("docs");
folderNames.forEach(System.out::println);

```

### 🔁 6. Copy a file

``` java
fileStorageService.copy("docs/manuals/guide.pdf", "backup/manuals/guide_backup.pdf");
```

### 🚚 7. Move a file

``` java
fileStorageService.move("docs/manuals/guide.pdf", "docs/archive/guide.pdf");

```

### 📝 8. Rename a file

``` java
fileStorageService.renameFile("docs/archive/guide.pdf", "guide-final.pdf");

```

### 📝 9. Rename a directory

``` java
fileStorageService.renameDirectory("docs/manuals", "docs/guides");

```

### ❌ 10. Delete a file

``` java
fileStorageService.delete("docs/archive/guide-final.pdf");
```

### ❌ 11. Delete a directory (with contents)

``` java
fileStorageService.delete("backup/manuals");
```

### 📄 12. Check existence

``` java
boolean exists = fileStorageService.exists("docs/guides");
```

### 📁 13. Is file?

``` java
boolean isFile = fileStorageService.isFile("docs/guides/guide.pdf");
```

### 📂 14. Is directory?

``` java
boolean isDir = fileStorageService.isDirectory("docs/guides");
```

---

## ✅ Tests

The module is completely **unit tested with Mockito**.

To run the tests :

```bash
mvn test
```
## 👤 About the Author
Developed with passion by Maximilien Kengne Kongne —
Senior Java backend developer.

Feel free to contact me : <a href="mailto:maximiliendenver@gmail.com">Maximilien KENGNE KONGNE</a>

---
## 🛠️ Contributions
We welcome contributions!
Feel free to open an issue or submit a pull request if you have feature requests or improvements.

---
## 📜 License
This project is licensed under the MIT License — you are free to use, modify, and distribute it.

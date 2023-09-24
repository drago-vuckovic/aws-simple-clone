package co.vuckovic.pegasus.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileVisitorUtil extends SimpleFileVisitor<Path> {

  @Override
  public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
      throws IOException {
    Files.delete(path);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path directory, IOException ioException)
      throws IOException {
    Files.delete(directory);
    return FileVisitResult.CONTINUE;
  }
}

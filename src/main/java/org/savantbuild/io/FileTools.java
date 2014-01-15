/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File utilities.
 *
 * @author Brian Pontarelli
 */
public class FileTools {
  /**
   * Copies the given Stream of paths to the target directory. For example:
   * <p>
   * <pre>
   *   copyRecursive(Files.list(Paths.get("foo")), Paths.get("bar"))
   * </pre>
   * <p>
   * This copies everything from the foo directory into the bar directory. This is the same as the command:
   * <p>
   * <pre>
   *   cp -r foo/* bar
   * </pre>
   *
   * @param stream The stream to copy the files from.
   * @param to     The location to copy the files to.
   * @throws IOException If the copy fails or the to directory cannot be create.
   */
  public static void copyRecursive(Stream<Path> stream, Path to) throws IOException {
    if (!Files.isDirectory(to)) {
      Files.createDirectories(to);
    }

    try (Stream<Path> tryStream = stream) {
      tryStream.forEach((path) -> {
        try {
          if (Files.isRegularFile(path)) {
            Files.copy(path, to.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
          } else if (Files.isDirectory(path)) {
            copyRecursive(Files.list(path), to.resolve(path.getFileName()));
          }
        } catch (IOException e) {
          // Wrap in a RuntimeException
          throw new IllegalStateException(e);
        }
      });
    } catch (IllegalStateException e) {
      throw (IOException) e.getCause();
    }
  }

  /**
   * Creates a temporary file.
   *
   * @param prefix       The prefix for the temporary file.
   * @param suffix       The suffix for the temporary file.
   * @param deleteOnExit If the file should be deleted when the JVM exits.
   * @return The Path of the temporary file.
   * @throws IOException If the create fails.
   */
  public static Path createTempPath(String prefix, String suffix, boolean deleteOnExit) throws IOException {
    File file = File.createTempFile(prefix, suffix);
    if (deleteOnExit) {
      file.deleteOnExit();
    }

    return file.toPath();
  }

  /**
   * Determines the files that have been modified with respect to the given output directory.
   *
   * @param rootDir   The root directory (used to calculate the full path to the files relative to this directory).
   * @param sourceDir The source directory to walk.
   * @param outputDir The output directory to compare against.
   * @param extension The extension required (i.e. .java)
   * @return The list of modified files.
   * @throws IllegalStateException If the code throws an IOException it is wrapped into an IllegalStateException.
   */
  public static List<String> modifiedFiles(Path rootDir, Path sourceDir, Path outputDir, String extension)
      throws IllegalStateException {
    Path projectSourceDir = rootDir.resolve(sourceDir);
    if (!Files.isDirectory(projectSourceDir)) {
      return Collections.emptyList();
    }

    Path projectOutputDir = rootDir.resolve(outputDir);
    try (Stream<Path> stream = Files.walk(projectSourceDir)) {
      return stream.filter((path) -> path.toString().endsWith(extension))
                   .map((path) -> path.subpath(rootDir.getNameCount(), path.getNameCount()))
                   .filter((path) -> isModified(path, projectOutputDir))
                   .map(Path::toString)
                   .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("Unable to determine which source files where changed", e);
    }
  }

  /**
   * Prunes the given path. If the path is a directory, this deletes everything underneath it, but does not traverse
   * across symbolic links, it simply deletes the link. If the path is a file, it is deleted. If the path is a symbolic
   * link, it is unlinked.
   *
   * @param path The path to delete.
   */
  public static void prune(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }

    if (Files.isSymbolicLink(path)) {
      Files.delete(path);
      return;
    }

    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Determines if the given path is modified when compared to the same path in the given outputDir.
   *
   * @param file      The file to check.
   * @param outputDir The output directory (used to build the output file).
   * @return True if the file is modified, false it if isn't.
   * @throws IllegalStateException If the check throws an IOException it is wrapped into an IllegalStateException.
   */
  private static boolean isModified(Path file, Path outputDir) {
    try {
      System.out.println("Comparing " + file + " to " + outputDir);
      Path outputFile = outputDir.resolve(file);
      return !Files.isRegularFile(outputFile) || Files.getLastModifiedTime(outputFile).toMillis() < Files.getLastModifiedTime(file).toMillis();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}

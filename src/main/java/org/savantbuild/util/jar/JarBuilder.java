/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
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
package org.savantbuild.util.jar;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.savantbuild.io.FileSet;

/**
 * Helps build Jar files.
 *
 * @author Brian Pontarelli
 */
public class JarBuilder {
  public final List<FileSet> fileSets = new ArrayList<>();

  public final Path file;

  public final Path baseDir;

  public JarBuilder(Path file, Path baseDir) {
    this.file = file;
    this.baseDir = baseDir;
  }

  public JarBuilder fileSet(FileSet fileSet) throws IOException {
    Path resolvedDirectory = fileSet.directory.isAbsolute() ? fileSet.directory : baseDir.resolve(fileSet.directory);
    if (Files.isRegularFile(resolvedDirectory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    if (!Files.isDirectory(resolvedDirectory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] does not exist");
    }

    fileSets.add(fileSet);
    return this;
  }

  public JarBuilder fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public JarBuilder fileSet(String directory) throws IOException {
    return fileSet(Paths.get(directory));
  }

  public JarBuilder optionalFileSet(FileSet fileSet) throws IOException {
    Path resolvedDirectory = fileSet.directory.isAbsolute() ? fileSet.directory : baseDir.resolve(fileSet.directory);
    if (Files.isRegularFile(resolvedDirectory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    // Only add if it exists
    if (Files.isDirectory(resolvedDirectory)) {
      fileSets.add(fileSet);
    }

    return this;
  }

  public JarBuilder optionalFileSet(Path directory) throws IOException {
    return optionalFileSet(new FileSet(directory));
  }

  public JarBuilder optionalFileSet(String directory) throws IOException {
    return optionalFileSet(Paths.get(directory));
  }

  public int build() throws IOException {
    if (!Files.isDirectory(file.getParent())) {
      Files.createDirectories(file.getParent());
    }

    AtomicInteger count = new AtomicInteger(0);
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(file))) {
      for (FileSet fileSet : fileSets) {
        Path resolvedDirectory = fileSet.directory.isAbsolute() ? fileSet.directory : baseDir.resolve(fileSet.directory);
        Files.walkFileTree(resolvedDirectory, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relativePath = file.subpath(resolvedDirectory.getNameCount(), file.getNameCount());
            JarEntry entry = new JarEntry(relativePath.toString());
            entry.setCreationTime((FileTime) Files.getAttribute(file, "creationTime"));
            entry.setLastAccessTime((FileTime) Files.getAttribute(file, "lastAccessTime"));
            entry.setLastModifiedTime(Files.getLastModifiedTime(file));
            entry.setTime(Files.getLastModifiedTime(file).toMillis());
            entry.setSize((Long) Files.getAttribute(file, "size"));
            jos.putNextEntry(entry);
            Files.copy(file, jos);
            count.incrementAndGet();
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }

    return count.get();
  }
}

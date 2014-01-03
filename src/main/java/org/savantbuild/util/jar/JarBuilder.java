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

import org.savantbuild.io.FileSet;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Helps build Jar files.
 *
 * @author Brian Pontarelli
 */
public class JarBuilder {
  public final List<FileSet> fileSets = new ArrayList<>();
  public final Path file;

  public JarBuilder(Path file) {
    this.file = file;
  }

  public JarBuilder fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path passed to the JarBuilder cannot be a file");
    }

    fileSets.add(fileSet);
    return this;
  }

  public JarBuilder fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public JarFile build() throws IOException {
    if (!Files.isDirectory(file.getParent())) {
      Files.createDirectories(file.getParent());
    }

    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(file))) {
      for (FileSet fileSet : fileSets) {
        Files.walkFileTree(fileSet.directory, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path relativePath = file.subpath(fileSet.directory.getNameCount(), file.getNameCount());
            JarEntry entry = new JarEntry(relativePath.toString());
            entry.setCreationTime((FileTime) Files.getAttribute(file, "creationTime"));
            entry.setLastAccessTime((FileTime) Files.getAttribute(file, "lastAccessTime"));
            entry.setLastModifiedTime(Files.getLastModifiedTime(file));
            entry.setTime(Files.getLastModifiedTime(file).toMillis());
            entry.setSize((Long) Files.getAttribute(file, "size"));
            jos.putNextEntry(entry);
            Files.copy(file, jos);
            return FileVisitResult.CONTINUE;
          }
        });
      }
    }

    return new JarFile(file.toFile());
  }
}

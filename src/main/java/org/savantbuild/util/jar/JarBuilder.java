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

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public JarBuilder(String file) {
    this.file = Paths.get(file);
  }

  public JarBuilder(Path file) {
    this.file = file;
  }

  public JarBuilder fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    if (!Files.isDirectory(fileSet.directory)) {
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
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    // Only add if it exists
    if (Files.isDirectory(fileSet.directory)) {
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
        fileSet.toFileInfos().forEach((info) -> {
          try {
            JarEntry entry = new JarEntry(info.relative.toString());
            entry.setCreationTime(info.creationTime);
            entry.setLastAccessTime(info.lastAccessTime);
            entry.setLastModifiedTime(info.lastModifiedTime);
            entry.setTime(info.lastModifiedTime.toMillis());
            entry.setSize(info.size);
            jos.putNextEntry(entry);
            Files.copy(info.origin, jos);
            jos.flush();
            jos.closeEntry();
            count.incrementAndGet();
          } catch (IOException e) {
            throw new IOError(e);
          }
        });
      }
    }

    return count.get();
  }
}

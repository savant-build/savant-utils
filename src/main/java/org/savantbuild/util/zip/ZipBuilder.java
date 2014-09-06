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
package org.savantbuild.util.zip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.savantbuild.io.FileInfo;
import org.savantbuild.io.FileSet;

/**
 * Helps build Zip files.
 *
 * @author Brian Pontarelli
 */
public class ZipBuilder {
  public final Path file;

  public final List<FileSet> fileSets = new ArrayList<>();

  public ZipBuilder(String file) {
    this(Paths.get(file));
  }

  public ZipBuilder(Path file) {
    this.file = file;
  }

  public int build() throws IOException {
    if (!Files.isDirectory(file.getParent())) {
      Files.createDirectories(file.getParent());
    }

    AtomicInteger count = new AtomicInteger(0);

    try (ZipOutputStream jos = new ZipOutputStream(Files.newOutputStream(file))) {
      for (FileSet fileSet : fileSets) {
        for (FileInfo fileInfo : fileSet.toFileInfos()) {
          ZipEntry entry = new ZipEntry(fileInfo.relative.toString());
          entry.setCreationTime(fileInfo.creationTime);
          entry.setLastAccessTime(fileInfo.lastAccessTime);
          entry.setLastModifiedTime(fileInfo.lastModifiedTime);
          entry.setTime(fileInfo.lastModifiedTime.toMillis());
          entry.setSize(fileInfo.size);
          jos.putNextEntry(entry);
          Files.copy(fileInfo.origin, jos);
          jos.flush();
          jos.closeEntry();
          count.incrementAndGet();
        }
      }
    }

    return count.get();
  }

  public ZipBuilder fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public ZipBuilder fileSet(String directory) throws IOException {
    return fileSet(Paths.get(directory));
  }

  public ZipBuilder fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    if (!Files.isDirectory(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] does not exist");
    }

    fileSets.add(fileSet);
    return this;
  }

  public ZipBuilder optionalFileSet(Path directory) throws IOException {
    return optionalFileSet(new FileSet(directory));
  }

  public ZipBuilder optionalFileSet(String directory) throws IOException {
    return optionalFileSet(Paths.get(directory));
  }

  public ZipBuilder optionalFileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    // Only add if it exists
    if (Files.isDirectory(fileSet.directory)) {
      fileSets.add(fileSet);
    }

    return this;
  }
}

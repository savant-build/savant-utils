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
package org.savantbuild.util.tar;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.savantbuild.io.FileInfo;
import org.savantbuild.io.FileSet;

/**
 * Helps build Tar files.
 *
 * @author Brian Pontarelli
 */
public class TarBuilder {
  public final Path file;

  public final List<FileSet> fileSets = new ArrayList<>();

  public boolean compress = true;

  public boolean storeGroup = false;

  public boolean storeOwner = false;

  public TarBuilder(String file) {
    this(Paths.get(file));
  }

  public TarBuilder(Path file) {
    this.file = file;

    // Guess at the compression
    this.compress = file.toString().endsWith(".gz");
  }

  public int build() throws IOException {
    if (Files.exists(file)) {
      Files.delete(file);
    }

    if (!Files.isDirectory(file.getParent())) {
      Files.createDirectories(file.getParent());
    }

    int count = 0;
    OutputStream os = Files.newOutputStream(file);
    try (TarArchiveOutputStream tos = new TarArchiveOutputStream(compress ? new GZIPOutputStream(os) : os)) {
      tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

      for (FileSet fileSet : fileSets) {
        for (FileInfo fileInfo : fileSet.toFileInfos()) {
          TarArchiveEntry entry = new TarArchiveEntry(fileInfo.relative.toString());
          entry.setModTime(fileInfo.lastModifiedTime.toMillis());
          if (storeGroup) {
            entry.setGroupName(fileInfo.groupName);
          }
          if (storeOwner) {
            entry.setUserName(fileInfo.userName);
          }
          entry.setSize(fileInfo.size);
          entry.setMode(fileInfo.toMode());
          tos.putArchiveEntry(entry);
          Files.copy(fileInfo.origin, tos);
          tos.closeArchiveEntry();
          count++;
        }
      }
    }

    return count;
  }

  public TarBuilder fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public TarBuilder fileSet(String directory) throws IOException {
    return fileSet(Paths.get(directory));
  }

  public TarBuilder fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] is a file and must be a directory");
    }

    if (!Files.isDirectory(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path [" + fileSet.directory + "] does not exist");
    }

    fileSets.add(fileSet);
    return this;
  }

  public TarBuilder optionalFileSet(Path directory) throws IOException {
    return optionalFileSet(new FileSet(directory));
  }

  public TarBuilder optionalFileSet(String directory) throws IOException {
    return optionalFileSet(Paths.get(directory));
  }

  public TarBuilder optionalFileSet(FileSet fileSet) throws IOException {
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

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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.savantbuild.io.Directory;
import org.savantbuild.io.FileInfo;
import org.savantbuild.io.FileSet;

/**
 * Helps build Jar files.
 *
 * @author Brian Pontarelli
 */
public class JarBuilder {
  public final List<Directory> directories = new ArrayList<>();

  public final Path file;

  public final List<FileSet> fileSets = new ArrayList<>();

  public Manifest manifest = new Manifest();

  public JarBuilder(String file) {
    this(Paths.get(file));
  }

  public JarBuilder(Path file) {
    this.file = file;
  }

  public int build() throws IOException {
    if (Files.exists(file)) {
      Files.delete(file);
    }

    if (!Files.isDirectory(file.getParent())) {
      Files.createDirectories(file.getParent());
    }

    int count = 0;

    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(file), manifest)) {
      for (Directory directory : directories) {
        String name = directory.name;
        JarEntry entry = new JarEntry(name.endsWith("/") ? name : name + "/");
        jos.putNextEntry(entry);
        jos.closeEntry();
        count++;
      }

      for (FileSet fileSet : fileSets) {
        for (FileInfo fileInfo : fileSet.toFileInfos()) {
          JarEntry entry = new JarEntry(fileInfo.relative.toString());
          entry.setCreationTime(fileInfo.creationTime);
          entry.setLastAccessTime(fileInfo.lastAccessTime);
          entry.setLastModifiedTime(fileInfo.lastModifiedTime);
          entry.setTime(fileInfo.lastModifiedTime.toMillis());
          entry.setSize(fileInfo.size);
          jos.putNextEntry(entry);
          Files.copy(fileInfo.origin, jos);
          jos.flush();
          jos.closeEntry();
          count++;
        }
      }
    }

    return count;
  }

  public JarBuilder directory(Directory directory) throws IOException {
    directories.add(directory);
    return this;
  }

  public JarBuilder ensureManifest(String vendor, String version) {
    manifest.getMainAttributes().putIfAbsent(Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().putIfAbsent(Name.IMPLEMENTATION_VENDOR, vendor);
    manifest.getMainAttributes().putIfAbsent(Name.IMPLEMENTATION_VERSION, version);
    manifest.getMainAttributes().putIfAbsent(Name.SPECIFICATION_VENDOR, vendor);
    manifest.getMainAttributes().putIfAbsent(Name.SPECIFICATION_VERSION, version);
    return this;
  }

  public JarBuilder fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public JarBuilder fileSet(String directory) throws IOException {
    return fileSet(Paths.get(directory));
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

  public JarBuilder manifest(Path file) throws IOException {
    try (InputStream is = Files.newInputStream(file)) {
      manifest.read(is);
    }
    return this;
  }

  public JarBuilder manifest(Map<String, Object> map) {
    Attributes attributes = manifest.getMainAttributes();
    map.forEach((key, value) -> attributes.put(new Attributes.Name(key), value.toString()));
    return this;
  }

  public JarBuilder optionalFileSet(Path directory) throws IOException {
    return optionalFileSet(new FileSet(directory));
  }

  public JarBuilder optionalFileSet(String directory) throws IOException {
    return optionalFileSet(Paths.get(directory));
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
}

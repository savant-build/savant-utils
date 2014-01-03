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

import org.savantbuild.output.Output;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides file copying utilities using relative or absolute paths. All relative paths are resolved using the
 * baseDirectory that is passed into the constructor.
 *
 * @author Brian Pontarelli
 */
public class Copier {
  public final Path baseDirectory;

  public final Output output;

  public Path to;

  public List<FileSet> fileSets = new ArrayList<>();

  public Copier(Output output) {
    this.output = output;
    this.baseDirectory = Paths.get("");
  }

  public Copier(Output output, Path baseDirectory) {
    this.output = output;
    this.baseDirectory = baseDirectory;
  }

  public int copy() throws IOException {
    Path absoluteTo = to.isAbsolute() ? to : baseDirectory.resolve(to);

    AtomicInteger count = new AtomicInteger(0);
    for (FileSet fileSet : fileSets) {
      Path absoluteFrom = fileSet.directory.isAbsolute() ? fileSet.directory : baseDirectory.resolve(fileSet.directory);
      if (!Files.isDirectory(absoluteFrom)) {
        continue;
      }

      Files.walkFileTree(absoluteFrom, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Path relativeDestination = file.subpath(absoluteFrom.getNameCount(), file.getNameCount());
          Path absoluteDestination = absoluteTo.resolve(relativeDestination);
          Files.createDirectories(absoluteDestination.getParent());
          Files.copy(file, absoluteDestination);
          count.incrementAndGet();
          return FileVisitResult.CONTINUE;
        }
      });
    }

    return count.get();
  }

  public Copier fileSet(FileSet fileSet) throws IOException {
    if (Files.isRegularFile(fileSet.directory)) {
      throw new IOException("The [fileSet.directory] path passed to the Copier cannot be a file");
    }

    this.fileSets.add(fileSet);
    return this;
  }

  public Copier fileSet(Path directory) throws IOException {
    return fileSet(new FileSet(directory));
  }

  public Copier to(Path to) throws IOException {
    if (Files.isRegularFile(to)) {
      throw new IOException("The [to] path passed to the Copier cannot be a file");
    }

    this.to = to;
    return this;
  }
}

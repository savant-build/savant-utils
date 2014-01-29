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
package org.savantbuild.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * A FileSet for archives. This allows the files in the FileSet to optionally contain a prefix.
 *
 * @author Brian Pontarelli
 */
public class ArchiveFileSet extends FileSet {
  public String prefix;

  /**
   * Constructs a new ArchiveFileSet. The directory is required but the prefix is optional. Leaving the prefix blank will
   * cause all of the files in the FileSet to contain relative paths based on the FileSet's directory. Using the prefix
   * will cause the files in the FileSet to be relative to the prefix plus the directory.
   *
   * @param directory The directory of the FileSet.
   * @param prefix The prefix used to calculate the relative paths in the FileInfo objects.
   */
  public ArchiveFileSet(Path directory, String prefix) {
    super(directory);
    this.prefix = prefix;
  }

  @Override
  public List<FileInfo> toFileInfos() throws IOException {
    List<FileInfo> infos = super.toFileInfos();
    if (prefix != null) {
      infos.forEach((info) -> info.relative = Paths.get(prefix, info.relative.toString()));
    }

    return infos;
  }
}

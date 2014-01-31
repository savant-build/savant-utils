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

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * The information about a file that is collected from a FileSet. This includes the origin Path (which might be absolute
 * or relative to the directory of a FileSet) and a relative path (to the FileSet directory). This might also include
 * other file attributes that are part of the calculation including mode, ownership, etc.
 *
 * @author Brian Pontarelli
 */
public class FileInfo {
  public FileTime creationTime;

  public FileTime lastAccessTime;

  public FileTime lastModifiedTime;

  public Path origin;

  public Set<PosixFilePermission> permissions;

  public Path relative;

  public Long size;

  public FileInfo(Path origin, Path relative) {
    this.origin = origin;
    this.relative = relative;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FileInfo fileInfo = (FileInfo) o;

    if (!creationTime.equals(fileInfo.creationTime)) return false;
    if (!lastAccessTime.equals(fileInfo.lastAccessTime)) return false;
    if (!lastModifiedTime.equals(fileInfo.lastModifiedTime)) return false;
    if (!origin.equals(fileInfo.origin)) return false;
    if (permissions != null ? !permissions.equals(fileInfo.permissions) : fileInfo.permissions != null) return false;
    if (!relative.equals(fileInfo.relative)) return false;
    if (!size.equals(fileInfo.size)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = creationTime.hashCode();
    result = 31 * result + lastAccessTime.hashCode();
    result = 31 * result + lastModifiedTime.hashCode();
    result = 31 * result + origin.hashCode();
    result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
    result = 31 * result + relative.hashCode();
    result = 31 * result + size.hashCode();
    return result;
  }

  /**
   * Converts the file permissions of this FileInfo to a POSIX bit mapped mode. The bit map looks like this:
   * <p>
   * <pre>
   *   1_000_000_001_000_000
   * </pre>
   * <p>
   * The first bit is always set. The next three bits are the set UID bits, the next 3 bits are the set GID bits. The
   * next three bits are the owner permissions (read, write, execute), then the group permissions and finally the user
   * permissions.
   *
   * @return The POSIX mode bit map as an integer.
   */
  public int toMode() {
    if (permissions == null || permissions.isEmpty()) {
      return 0b1_000_000_110_100_100;
    }

    int mode = 0b1_000_000_000_000_000;
    for (PosixFilePermission permission : permissions) {
      switch (permission) {
        case GROUP_EXECUTE:
          mode |= 0b001_000;
          break;
        case GROUP_READ:
          mode |= 0b100_000;
          break;
        case GROUP_WRITE:
          mode |= 0b010_000;
          break;
        case OTHERS_EXECUTE:
          mode |= 0b001;
          break;
        case OTHERS_READ:
          mode |= 0b100;
          break;
        case OTHERS_WRITE:
          mode |= 0b010;
          break;
        case OWNER_EXECUTE:
          mode |= 0b001_000_000;
          break;
        case OWNER_READ:
          mode |= 0b100_000_000;
          break;
        case OWNER_WRITE:
          mode |= 0b010_000_000;
          break;
      }
    }

    return mode;
  }
}

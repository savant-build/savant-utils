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

  public int toMode() {
    if (permissions == null || permissions.isEmpty()) {
      return 666;
    }

    int mode = 0;
    for (PosixFilePermission permission : permissions) {
      switch(permission) {
        case GROUP_EXECUTE:
          mode += 10;
          break;
        case GROUP_READ:
          mode += 40;
          break;
        case GROUP_WRITE:
          mode += 20;
          break;
        case OTHERS_EXECUTE:
          mode += 1;
          break;
        case OTHERS_READ:
          mode += 4;
          break;
        case OTHERS_WRITE:
          mode += 2;
          break;
        case OWNER_EXECUTE:
          mode += 100;
          break;
        case OWNER_READ:
          mode += 400;
          break;
        case OWNER_WRITE:
          mode += 200;
          break;
      }
    }

    return mode;
  }
}

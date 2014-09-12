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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests the Tar tools.
 *
 * @author Brian Pontarelli
 */
public class TarToolsTest extends BaseUnitTest {
  @Test
  public void untar() throws Exception {
    FileTools.prune(projectDir.resolve("build/test"));
    Path testFile = projectDir.resolve("build/test/test.tar");
    TarBuilder builder = new TarBuilder(testFile);
    builder.storeGroup = true;
    builder.storeOwner = true;
    builder.fileSet(projectDir.resolve("src/main/java"));
    builder.build();

    Path untarDir = projectDir.resolve("build/test/untar");
    TarTools.untar(testFile, untarDir, true, true);
    Files.walk(untarDir).forEach((file) -> {
      if (Files.isDirectory(file)) {
        return;
      }

      Path source = projectDir.resolve("src/main/java").resolve(untarDir.relativize(file));
      try {
        assertEquals(Files.readAllBytes(file), Files.readAllBytes(source), "Files aren't equal [" + source + "] and [" + file + "]");

        int sourceMode = FileTools.toMode(Files.getPosixFilePermissions(source));
        int fileMode = FileTools.toMode(Files.getPosixFilePermissions(file));
        assertEquals(fileMode, sourceMode);

        String sourceOwner = Files.getOwner(source).getName();
        String fileOwner = Files.getOwner(file).getName();
        assertEquals(fileOwner, sourceOwner);

        String sourceGroup = Files.readAttributes(source, PosixFileAttributes.class).group().getName();
        String fileGroup = Files.readAttributes(file, PosixFileAttributes.class).group().getName();
        assertEquals(sourceGroup, fileGroup);
        assertEquals(sourceGroup, fileGroup);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    });

    // Do it again and ensure things don't blow up
    TarTools.untar(testFile, untarDir, true, true);
  }

  @Test
  public void untar_compress() throws Exception {
    FileTools.prune(projectDir.resolve("build/test"));
    Path testFile = projectDir.resolve("build/test/test.tar.gz");
    TarBuilder builder = new TarBuilder(testFile);
    builder.compress = true;
    builder.fileSet(projectDir.resolve("src/main/java"));
    builder.build();

    Path untarDir = projectDir.resolve("build/test/untar");
    TarTools.untar(testFile, untarDir, false, false);
    Files.walk(untarDir).forEach((file) -> {
      if (Files.isDirectory(file)) {
        return;
      }

      Path source = projectDir.resolve("src/main/java").resolve(untarDir.relativize(file));
      try {
        assertEquals(Files.readAllBytes(file), Files.readAllBytes(source), "Files aren't equal [" + source + "] and [" + file + "]");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    // Do it again and ensure things don't blow up
    TarTools.untar(testFile, untarDir, false, false);
  }
}

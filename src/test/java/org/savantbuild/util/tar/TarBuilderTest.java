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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.savantbuild.BaseUnitTest;
import org.savantbuild.io.Directory;
import org.savantbuild.io.FileSet;
import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests the TarBuilder.
 *
 * @author Brian Pontarelli
 */
public class TarBuilderTest extends BaseUnitTest {
  private static void assertTarFileEquals(Path tarFile, String entry, Path original) throws IOException {
    InputStream is = Files.newInputStream(tarFile);
    if (tarFile.toString().endsWith(".gz")) {
      is = new GZIPInputStream(is);
    }

    TarArchiveInputStream tis = new TarArchiveInputStream(is);
    TarArchiveEntry tarArchiveEntry = tis.getNextTarEntry();
    while (tarArchiveEntry != null && !tarArchiveEntry.getName().equals(entry)) {
      tarArchiveEntry = tis.getNextTarEntry();
    }

    if (tarArchiveEntry == null) {
      fail("Tar [" + tarFile + "] is missing entry [" + entry + "]");
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int length;
    while ((length = tis.read(buf)) != -1) {
      baos.write(buf, 0, length);
    }

    assertEquals(Files.readAllBytes(original), baos.toByteArray());
    assertEquals(tarArchiveEntry.getSize(), Files.size(original));
    assertEquals(tarArchiveEntry.getUserName(), Files.getOwner(original).getName());
    assertEquals(tarArchiveEntry.getGroupName(), Files.readAttributes(original, PosixFileAttributes.class).group().getName());
  }

  private static void assertTarContainsDirectory(Path tarFile, String entry, Integer mode, String userName, String groupName) throws IOException {
    InputStream is = Files.newInputStream(tarFile);
    if (tarFile.toString().endsWith(".gz")) {
      is = new GZIPInputStream(is);
    }

    TarArchiveInputStream tis = new TarArchiveInputStream(is);
    TarArchiveEntry tarArchiveEntry = tis.getNextTarEntry();
    while (tarArchiveEntry != null && !tarArchiveEntry.getName().equals(entry)) {
      tarArchiveEntry = tis.getNextTarEntry();
    }

    if (tarArchiveEntry == null) {
      fail("Tar [" + tarFile + "] is missing entry [" + entry + "]");
    }

    assertTrue(tarArchiveEntry.isDirectory());
    if (mode != null) {
      assertEquals(tarArchiveEntry.getMode(), FileTools.toMode(mode));
    }
    if (userName != null) {
      assertEquals(tarArchiveEntry.getUserName(), userName);
    }
    if (groupName != null) {
      assertEquals(tarArchiveEntry.getGroupName(), groupName);
    }
  }

  @Test
  public void build() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/tars"));

    Path file = projectDir.resolve("build/test/tars/test.tar");
    TarBuilder builder = new TarBuilder(file);
    builder.storeGroupName = true;
    builder.storeUserName = true;
    int count = builder.fileSet(new FileSet(projectDir.resolve("src/main/java")))
                       .fileSet(new FileSet(projectDir.resolve("src/test/java")))
                       .optionalFileSet(new FileSet(projectDir.resolve("doesNotExist")))
                       .directory(new Directory("test/directory", 0x755, "root", "root"))
                       .build();
    assertTrue(Files.isReadable(file));
    assertTarFileEquals(file, "org/savantbuild/io/Copier.java", projectDir.resolve("src/main/java/org/savantbuild/io/Copier.java"));
    assertTarFileEquals(file, "org/savantbuild/io/FileSet.java", projectDir.resolve("src/main/java/org/savantbuild/io/FileSet.java"));
    assertTarContainsDirectory(file, "test/directory/", 0x755, "root", "root");
    assertEquals(count, 45);
  }

  @Test
  public void buildCompress() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/tars"));

    Path file = projectDir.resolve("build/test/tars/test.tar.gz");
    TarBuilder builder = new TarBuilder(file);
    builder.storeGroupName = true;
    builder.storeUserName = true;
    builder.compress = true;
    int count = builder.fileSet(new FileSet(projectDir.resolve("src/main/java")))
                       .fileSet(new FileSet(projectDir.resolve("src/test/java")))
                       .optionalFileSet(new FileSet(projectDir.resolve("doesNotExist")))
                       .build();
    assertTrue(Files.isReadable(file));
    assertTarFileEquals(file, "org/savantbuild/io/Copier.java", projectDir.resolve("src/main/java/org/savantbuild/io/Copier.java"));
    assertTarFileEquals(file, "org/savantbuild/io/FileSet.java", projectDir.resolve("src/main/java/org/savantbuild/io/FileSet.java"));
    assertEquals(count, 44);
  }

  @Test
  public void buildRequiredDirectoryFailure() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/tars"));

    Path file = projectDir.resolve("build/test/tars/test.tar");
    TarBuilder builder = new TarBuilder(file);
    try {
      builder.fileSet(new FileSet(projectDir.resolve("src/main/java")))
             .fileSet(new FileSet(projectDir.resolve("src/test/java")))
             .fileSet(new FileSet(projectDir.resolve("doesNotExist")))
             .build();
      fail("Should have failed");
    } catch (IOException e) {
      // Expected
    }
  }

  @Test
  public void buildStrings() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/tars"));

    Path file = projectDir.resolve("build/test/tars/test.tar.gz");
    TarBuilder builder = new TarBuilder(file.toString());
    builder.storeGroupName = true;
    builder.storeUserName = true;
    int count = builder.fileSet(projectDir.resolve("src/main/java").toString())
                       .fileSet(projectDir.resolve("src/test/java").toString())
                       .optionalFileSet("doesNotExist")
                       .build();
    assertTrue(Files.isReadable(file));
    assertTarFileEquals(file, "org/savantbuild/io/Copier.java", projectDir.resolve("src/main/java/org/savantbuild/io/Copier.java"));
    assertTarFileEquals(file, "org/savantbuild/io/FileSet.java", projectDir.resolve("src/main/java/org/savantbuild/io/FileSet.java"));
    assertEquals(count, 44);
  }
}

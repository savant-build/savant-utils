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

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.savantbuild.BaseUnitTest;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

/**
 * Tests the ArchiveFileSet class.
 *
 * @author Brian Pontarelli
 */
public class ArchiveFileSetTest extends BaseUnitTest {
  @Test
  public void toFileInfosNoPrefix() throws Exception {
    ArchiveFileSet fileSet = new ArchiveFileSet(projectDir.resolve("src/main/java"), null);
    List<FileInfo> infos = fileSet.toFileInfos();
    assertEquals(infos.stream().map((info) -> info.origin).collect(Collectors.toList()), asList(
        projectDir.resolve("src/main/java/org/savantbuild/io/ArchiveFileSet.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/Copier.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileInfo.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileSet.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/IOTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/Classpath.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/RuntimeTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/StringTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/net/NetTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/Ansi256Colors.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/Output.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/SystemOutOutput.java"),
        projectDir.resolve("src/main/java/org/savantbuild/security/MD5.java"),
        projectDir.resolve("src/main/java/org/savantbuild/security/MD5Exception.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/CyclicException.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/Graph.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/HashGraph.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/jar/JarBuilder.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/zip/ZipBuilder.java")
    ));
    assertEquals(infos.stream().map((info) -> info.relative).collect(Collectors.toList()), asList(
        Paths.get("org/savantbuild/io/ArchiveFileSet.java"),
        Paths.get("org/savantbuild/io/Copier.java"),
        Paths.get("org/savantbuild/io/FileInfo.java"),
        Paths.get("org/savantbuild/io/FileSet.java"),
        Paths.get("org/savantbuild/io/FileTools.java"),
        Paths.get("org/savantbuild/io/IOTools.java"),
        Paths.get("org/savantbuild/lang/Classpath.java"),
        Paths.get("org/savantbuild/lang/RuntimeTools.java"),
        Paths.get("org/savantbuild/lang/StringTools.java"),
        Paths.get("org/savantbuild/net/NetTools.java"),
        Paths.get("org/savantbuild/output/Ansi256Colors.java"),
        Paths.get("org/savantbuild/output/Output.java"),
        Paths.get("org/savantbuild/output/SystemOutOutput.java"),
        Paths.get("org/savantbuild/security/MD5.java"),
        Paths.get("org/savantbuild/security/MD5Exception.java"),
        Paths.get("org/savantbuild/util/CyclicException.java"),
        Paths.get("org/savantbuild/util/Graph.java"),
        Paths.get("org/savantbuild/util/HashGraph.java"),
        Paths.get("org/savantbuild/util/jar/JarBuilder.java"),
        Paths.get("org/savantbuild/util/zip/ZipBuilder.java")
    ));
  }

  @Test
  public void toFileInfosWithPrefix() throws Exception {
    ArchiveFileSet fileSet = new ArchiveFileSet(projectDir.resolve("src/main/java"), "some-directory-1.0");
    List<FileInfo> infos = fileSet.toFileInfos();
    assertEquals(infos.stream().map((info) -> info.origin).collect(Collectors.toList()), asList(
        projectDir.resolve("src/main/java/org/savantbuild/io/ArchiveFileSet.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/Copier.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileInfo.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileSet.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/FileTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/io/IOTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/Classpath.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/RuntimeTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/lang/StringTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/net/NetTools.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/Ansi256Colors.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/Output.java"),
        projectDir.resolve("src/main/java/org/savantbuild/output/SystemOutOutput.java"),
        projectDir.resolve("src/main/java/org/savantbuild/security/MD5.java"),
        projectDir.resolve("src/main/java/org/savantbuild/security/MD5Exception.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/CyclicException.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/Graph.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/HashGraph.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/jar/JarBuilder.java"),
        projectDir.resolve("src/main/java/org/savantbuild/util/zip/ZipBuilder.java")
    ));
    assertEquals(infos.stream().map((info) -> info.relative).collect(Collectors.toList()), asList(
        Paths.get("some-directory-1.0/org/savantbuild/io/ArchiveFileSet.java"),
        Paths.get("some-directory-1.0/org/savantbuild/io/Copier.java"),
        Paths.get("some-directory-1.0/org/savantbuild/io/FileInfo.java"),
        Paths.get("some-directory-1.0/org/savantbuild/io/FileSet.java"),
        Paths.get("some-directory-1.0/org/savantbuild/io/FileTools.java"),
        Paths.get("some-directory-1.0/org/savantbuild/io/IOTools.java"),
        Paths.get("some-directory-1.0/org/savantbuild/lang/Classpath.java"),
        Paths.get("some-directory-1.0/org/savantbuild/lang/RuntimeTools.java"),
        Paths.get("some-directory-1.0/org/savantbuild/lang/StringTools.java"),
        Paths.get("some-directory-1.0/org/savantbuild/net/NetTools.java"),
        Paths.get("some-directory-1.0/org/savantbuild/output/Ansi256Colors.java"),
        Paths.get("some-directory-1.0/org/savantbuild/output/Output.java"),
        Paths.get("some-directory-1.0/org/savantbuild/output/SystemOutOutput.java"),
        Paths.get("some-directory-1.0/org/savantbuild/security/MD5.java"),
        Paths.get("some-directory-1.0/org/savantbuild/security/MD5Exception.java"),
        Paths.get("some-directory-1.0/org/savantbuild/util/CyclicException.java"),
        Paths.get("some-directory-1.0/org/savantbuild/util/Graph.java"),
        Paths.get("some-directory-1.0/org/savantbuild/util/HashGraph.java"),
        Paths.get("some-directory-1.0/org/savantbuild/util/jar/JarBuilder.java"),
        Paths.get("some-directory-1.0/org/savantbuild/util/zip/ZipBuilder.java")
    ));
  }
}

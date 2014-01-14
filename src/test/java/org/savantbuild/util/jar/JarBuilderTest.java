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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.io.FileSet;
import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import static java.util.Arrays.stream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the JarBuilder.
 *
 * @author Brian Pontarelli
 */
public class JarBuilderTest extends BaseUnitTest {
  private static void assertJarContains(JarFile jarFile, String... entries) {
    stream(entries).forEach((entry) -> assertNotNull(jarFile.getEntry(entry), "Jar [" + jarFile + "] is missing entry [" + entry + "]"));
  }

  @Test
  public void build() throws Exception {
    System.out.println(projectDir.toAbsolutePath());
    FileTools.prune(projectDir.resolve("build/test/jars"));

    Path path = projectDir.resolve("build/test/jars/test.jar");
    JarBuilder builder = new JarBuilder(path, projectDir);
    int count = builder.fileSet(new FileSet(Paths.get("src/main/java")))
                       .fileSet(new FileSet(Paths.get("src/test/java")))
                       .build();
    assertTrue(Files.isReadable(path));
    assertJarContains(new JarFile(path.toFile()), "org/savantbuild/io/Copier.java", "org/savantbuild/io/CopierTest.java",
        "org/savantbuild/io/FileSet.java", "org/savantbuild/io/FileTools.java");
    assertEquals(count, 24);
  }
}

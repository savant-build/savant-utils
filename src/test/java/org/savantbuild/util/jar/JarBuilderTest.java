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

import org.savantbuild.BaseTest;
import org.savantbuild.io.FileSet;
import org.savantbuild.io.FileTools;
import org.testng.annotations.Test;

import java.util.jar.JarFile;

import static java.util.Arrays.stream;
import static org.testng.Assert.assertNotNull;

/**
 * Tests the JarBuilder.
 *
 * @author Brian Pontarelli
 */
public class JarBuilderTest extends BaseTest {
  @Test
  public void build() throws Exception {
    FileTools.prune(projectDir.resolve("build/test/jars"));

    JarBuilder builder = new JarBuilder(projectDir.resolve("build/test/jars/test.jar"));
    JarFile jar = builder.fileSet(new FileSet(projectDir.resolve("src/main/java")))
                         .fileSet(new FileSet(projectDir.resolve("src/test/java")))
                         .build();
    assertJarContains(jar, "org/savantbuild/io/Copier.java", "org/savantbuild/io/CopierTest.java",
        "org/savantbuild/io/FileSet.java", "org/savantbuild/io/FileTools.java");
  }

  private static void assertJarContains(JarFile jarFile, String... entries) {
    stream(entries).forEach((entry) -> assertNotNull(jarFile.getEntry(entry), "Jar [" + jarFile + "] is missing entry [" + entry + "]"));
  }
}

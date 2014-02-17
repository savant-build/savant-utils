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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.savantbuild.BaseUnitTest;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the Copier.
 *
 * @author Brian Pontarelli
 */
@Test(groups = "unit")
public class CopierTest extends BaseUnitTest {
  @Test
  public void copyEverything() throws Exception {
    Path toDir = projectDir.resolve("build/test/copy");
    FileTools.prune(toDir);

    Copier copier = new Copier(projectDir.resolve("build/test/copy"));
    copier.fileSet(projectDir.resolve("src/main/java"))
          .copy();

    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/Copier.java")));
    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/FileTools.java")));
    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/lang/Classpath.java")));
  }

  @Test
  public void copyIncludePatterns() throws Exception {
    Path toDir = projectDir.resolve("build/test/copy");
    FileTools.prune(toDir);

    Copier copier = new Copier(projectDir.resolve("build/test/copy"));
    copier.fileSet(new FileSet(projectDir.resolve("src/main/java"), asList(Pattern.compile(".*/io/.*"))))
        .copy();

    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/Copier.java")));
    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/FileTools.java")));
    assertFalse(Files.isRegularFile(toDir.resolve("org/savantbuild/lang/Classpath.java")));
  }
}
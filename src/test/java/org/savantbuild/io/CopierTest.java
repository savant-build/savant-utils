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

import org.savantbuild.BaseUnitTest;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    Copier copier = new Copier(projectDir);
    copier.to(Paths.get("build/test/copy"))
          .fileSet(Paths.get("src/main/java"))
          .copy();

    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/Copier.java")));
    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/io/FileTools.java")));
    assertTrue(Files.isRegularFile(toDir.resolve("org/savantbuild/lang/Classpath.java")));
  }
}

/*
 * Copyright (c) 2013, Inversoft Inc., All Rights Reserved
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

import org.savantbuild.BaseTest;
import org.testng.annotations.Test;

import java.nio.file.Files;

import static org.testng.Assert.assertTrue;

/**
 * Tests the FileTools.
 *
 * @author Brian Pontarelli
 */
public class FileToolsTest extends BaseTest {
  @Test
  public void copy() throws Exception {
    FileTools.copyRecursive(Files.list(projectDir.resolve("src/test/java")), projectDir.resolve("build/test"));
    assertTrue(Files.isDirectory(projectDir.resolve("build/test/org")));
    assertTrue(Files.isRegularFile(projectDir.resolve("build/test/org/savantbuild/io/FileToolsTest.java")));
    assertTrue(Files.isRegularFile(projectDir.resolve("build/test/org/savantbuild/output/SystemOutOutputTest.java")));
  }
}

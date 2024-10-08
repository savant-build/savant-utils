/*
 * Copyright (c) 2001-2024, Inversoft Inc., All Rights Reserved
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
package org.savantbuild.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.savantbuild.BaseUnitTest;
import org.savantbuild.security.MD5;
import org.savantbuild.security.MD5Exception;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.sun.net.httpserver.HttpServer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * The net tools test.
 */
public class NetToolsTest extends BaseUnitTest {
  @Test
  public void build() throws URISyntaxException {
    URI uri = NetTools.build("http://www.example.com", "org/apache/commons", "common-collections", "3.0", "commons-collections-3.0.jar");
    assertEquals(uri.toString(), "http://www.example.com/org/apache/commons/common-collections/3.0/commons-collections-3.0.jar");

    uri = NetTools.build("http://www.example.com/", "org/apache/commons", "common-collections", "3.0", "commons-collections-3.0.jar");
    assertEquals(uri.toString(), "http://www.example.com/org/apache/commons/common-collections/3.0/commons-collections-3.0.jar");

    uri = NetTools.build("http://www.example.com/", "/org/apache/commons/", "common-collections", "3.0", "commons-collections-3.0.jar");
    assertEquals(uri.toString(), "http://www.example.com/org/apache/commons/common-collections/3.0/commons-collections-3.0.jar");
  }

  @Test
  public void downloadToFile() throws Exception {
    HttpServer server = makeFileServer(null, null);

    try {
      Path path = NetTools.downloadToPath(new URI("http://localhost:7042/src/test/java/org/savantbuild/net/TestFile.txt"), null, null, null);
      String result = new String(Files.readAllBytes(path), "UTF-8");
      assertEquals(result.trim(), "This file is a test file for copying and writing and such.");
    } finally {
      server.stop(0);
    }
  }

  @Test
  public void downloadToFileFromFile() throws Exception {
    var fileURI = projectDir.resolve("src/test/java/org/savantbuild/net/TestFile.txt")
                            .toUri();
    Path path = NetTools.downloadToPath(fileURI, null, null, null);
    String result = new String(Files.readAllBytes(path), "UTF-8");
    assertEquals(result.trim(), "This file is a test file for copying and writing and such.");
  }

  @Test
  public void downloadToFileWithMD5() throws Exception {
    HttpServer server = makeFileServer(null, null);

    try {
      MD5 md5 = MD5.forBytes(Files.readAllBytes(projectDir.resolve("src/test/java/org/savantbuild/net/TestFile.txt")), "TestFile.txt");
      Path path = NetTools.downloadToPath(new URI("http://localhost:7042/src/test/java/org/savantbuild/net/TestFile.txt"), null, null, md5);
      String result = new String(Files.readAllBytes(path), "UTF-8");
      assertEquals(result.trim(), "This file is a test file for copying and writing and such.");
    } finally {
      server.stop(0);
    }
  }

  @Test
  public void downloadToFileWithMD5Failure() throws Exception {
    HttpServer server = makeFileServer(null, null);
    MD5 md5 = new MD5("0000000000000000000000000000000", new byte[]{0, 0, 0, 0, 0}, null);
    try {
      NetTools.downloadToPath(new URI("http://localhost:7042/src/test/java/org/savantbuild/net/TestFile.txt"), null, null, md5);
      fail("Should have failed");
    } catch (MD5Exception e) {
      // Expected
    } finally {
      server.stop(0);
    }
  }

  @Test
  public void downloadToFileWithUsernameAndPassword() throws Exception {
    HttpServer server = makeFileServer("User", "Pass");

    try {
      Path path = NetTools.downloadToPath(new URI("http://localhost:7042/src/test/java/org/savantbuild/net/TestFile.txt"), "User", "Pass", null);
      String result = new String(Files.readAllBytes(path), "UTF-8");
      assertEquals(result.trim(), "This file is a test file for copying and writing and such.");
    } finally {
      server.stop(0);
    }
  }

  /**
   * Creates a file server that will accept HTTP connections on localhost:7042 and return the bytes of the file in the
   * request starting from the project directory.
   *
   * @param username (Optional) The username to verify was sent to the server in the Authentication header. Leave blank
   *                 to not check.
   * @param password (Optional) The password to verify was sent to the server in the Authentication header. Leave blank
   *                 to not check.
   * @return The server.
   * @throws IOException If the server could not be created.
   */
  protected HttpServer makeFileServer(String username, String password) throws IOException {
    InetSocketAddress localhost = new InetSocketAddress(7042);
    HttpServer server = HttpServer.create(localhost, 0);
    server.createContext("/", (httpExchange) -> {
      if (username != null) {
        assertEquals(httpExchange.getRequestHeaders().get("Authorization").get(0), "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
      }

      // Verify a GET request
      if (!httpExchange.getRequestMethod().equals("GET")) {
        AssertJUnit.fail("Should have been a GET request");
      }

      // Close the input stream because we don't need to read anything
      httpExchange.getRequestBody().close();

      String path = httpExchange.getRequestURI().getPath();
      Path file = projectDir.resolve(path.substring(1));
      if (Files.isRegularFile(file)) {
        httpExchange.sendResponseHeaders(200, Files.size(file));
        byte[] bytes = Files.readAllBytes(file);
        httpExchange.getResponseBody().write(bytes);
        httpExchange.getResponseBody().flush();
        httpExchange.getResponseBody().close();
      } else {
        httpExchange.sendResponseHeaders(404, 0);
      }
    });

    server.start();

    return server;
  }
}

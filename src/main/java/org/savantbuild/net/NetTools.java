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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

import org.savantbuild.security.MD5;
import org.savantbuild.security.MD5Exception;
import org.savantbuild.security.MD5Tools;

/**
 * This class provides toolkit methods for helping work with URLs and URIs and other network classes.
 *
 * @author Brian Pontarelli
 */
public class NetTools {
  // better to not constantly instantiate new clients
  private static final HttpClient httpClient = HttpClient.newBuilder()
                                                         .connectTimeout(Duration.ofMillis(4000))
                                                         .followRedirects(Redirect.NORMAL)
                                                         .build();

  /**
   * Builds a URI from the given parts. These are concatenated together with slashes, depending on the endings of each.
   *
   * @param parts The parts
   * @return The URI.
   */
  public static URI build(String... parts) {
    StringBuilder build = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      boolean endSlash = build.charAt(build.length() - 1) == '/';
      boolean startSlash = parts[i].startsWith("/");
      if (!endSlash && !startSlash) {
        build.append("/");
      }

      String part = parts[i];
      if (endSlash && startSlash) {
        part = parts[i].substring(1);
      }

      boolean first = true;
      String[] splits = part.split("/");
      for (String split : splits) {
        if (!first) {
          build.append("/");
        } else {
          first = false;
        }

        try {
          build.append(URLEncoder.encode(split, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          // Not really possible
          throw new IllegalStateException("The JVM doesn't have UTF-8", e);
        }
      }
    }

    return URI.create(build.toString());
  }

  /**
   * Downloads the resource given.
   *
   * @param uri      The resource.
   * @param username (Optional) The username that might be used to connect to the resource.
   * @param password (Optional) The password that might be used to connect to the resource.
   * @param md5      (Optional) The MD5 of the resource (to verify).
   * @return A temp file that stores the resource or null if the given URI doesn't exist.
   * @throws IOException If the resource could not be downloaded.
   * @throws MD5Exception If the file was downloaded but doesn't match the MD5 sum.
   */
  public static Path downloadToPath(URI uri, String username, String password, MD5 md5) throws IOException, MD5Exception, InterruptedException {
    InputStream inputStream = uri.getScheme().startsWith("http") ? fetchViaHttp(uri, username, password) :
        fetchFile(uri);
    File file = File.createTempFile("savant-net-tools", "download");
    file.deleteOnExit();

    try (InputStream is = inputStream; FileOutputStream os = new FileOutputStream(file)) {
      MD5Tools.write(is, os, md5);
      os.flush();
    }

    return file.toPath();
  }

  private static InputStream fetchFile(URI uri) throws IOException {
    URLConnection uc = uri.toURL().openConnection();
    uc.setConnectTimeout(4000);
    uc.setReadTimeout(10000);
    uc.setDoInput(true);
    uc.setDoOutput(false);
    uc.connect();
    return uc.getInputStream();
  }

  private static InputStream fetchViaHttp(URI uri, String username, String password) throws IOException, InterruptedException {
    var requestBuilder = HttpRequest.newBuilder()
                                    .uri(uri)
                                    .GET()
                                    .timeout(Duration.ofMillis(10000));
    if (username != null) {
      String credentials = username + ":" + password;
      requestBuilder.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
    }

    var response = httpClient.send(requestBuilder.build(), BodyHandlers.ofInputStream());

    int result = response.statusCode();
    if (result != 200 && result != 404 && result != 410) {
      throw new IOException("HTTP sent an unexpected response code [" + result + "]");
    } else if (result == 404 || result == 410) {
      return null;
    }
    return response.body();
  }
}

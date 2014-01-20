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
package org.savantbuild.output;

import java.io.PrintStream;

/**
 * Basic Savant output system. This writes to Appendables.
 *
 * @author Brian Pontarelli
 */
public class SystemOutOutput implements Output {
  private final boolean colorize;

  private final PrintStream out;

  private boolean debugEnabled;

  public SystemOutOutput(boolean colorize) {
    this.out = System.out;
    this.colorize = colorize;
  }

  SystemOutOutput(PrintStream out, boolean colorize) {
    this.out = out;
    this.colorize = colorize;
  }

  public void debug(String message, Object... values) {
    if (debugEnabled) {
      println(message, values);
    }
  }

  @Override
  public void debug(Throwable t) {
    if (debugEnabled) {
      t.printStackTrace(out);
    }
  }

  @Override
  public void disableDebug() {
    this.debugEnabled = false;
  }

  @Override
  public void enableDebug() {
    this.debugEnabled = true;
  }

  public void error(String message, Object... values) {
    if (colorize) {
      Ansi256Colors.setColor(out, Ansi256Colors.ERROR);
    }

    println(message, values);

    if (colorize) {
      Ansi256Colors.clear(out);
    }
  }

  public void info(String message, Object... values) {
    println(message, values);
  }

  public void warning(String message, Object... values) {
    if (colorize) {
      Ansi256Colors.setColor(out, Ansi256Colors.WARNING);
    }

    println(message, values);

    if (colorize) {
      Ansi256Colors.clear(out);
    }
  }

  private void println(String message, Object[] values) {
    if (values.length == 0) {
      out.println(message);
    } else {
      out.printf(message + "\n", values);
    }
  }
}

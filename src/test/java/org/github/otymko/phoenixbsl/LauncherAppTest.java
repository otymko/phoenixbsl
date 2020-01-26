package org.github.otymko.phoenixbsl;

import org.junit.Test;

import java.nio.file.Path;

public class LauncherAppTest {

  @Test
  public void test() {

    var a = System.getenv("APPDATA");

    var pathToLog = Path.of(System.getenv("APPDATA"), "PhoenixBSL", "log.txt");
    pathToLog.toFile().mkdirs();

  }

}

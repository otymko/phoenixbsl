package com.github.otymko.phoenixbsl.logic.utils;

import com.github.otymko.phoenixbsl.model.Configuration;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

@UtilityClass
public class ProcessHelper {

  public Collection<String> getArgumentsRunProcessBSLLS(Configuration configuration) {
    var pathToBSLLS = Path.of(configuration.getPathToBSLLS()).toAbsolutePath();

    Collection<String> arguments = new ArrayList<>();
    if (configuration.isUsePathToJarBSLLS()) {
      arguments.add(configuration.getPathToJava());
      arguments.add("-jar");
    }
    arguments.add(pathToBSLLS.toString());

    return arguments;
  }

  @SneakyThrows
  public String getStdoutProcess(Process process) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = bufferedReader.readLine();
    process.waitFor();
    bufferedReader.close();
    return line;
  }

}

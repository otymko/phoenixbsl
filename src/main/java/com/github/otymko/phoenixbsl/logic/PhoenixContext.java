package com.github.otymko.phoenixbsl.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.otymko.phoenixbsl.PhoenixCore;
import com.github.otymko.phoenixbsl.model.Configuration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class PhoenixContext {
  public static final String BSL_CONFIGURATION_NAME = ".bsl-language-server.json";
  private final PhoenixCore core;
  @Getter
  private final Path basePathApp = Path.of(System.getProperty("user.home"), PhoenixCore.APPLICATION_NAME);
  @Getter
  private final Path pathToFolderLog = createPathToLog();
  @Getter
  private final Path pathToConfiguration = createPathToConfiguration();
  @Getter
  private final Path pathToBSLConfigurationDefault = Path.of(basePathApp.toString(), BSL_CONFIGURATION_NAME);

  public PhoenixContext(PhoenixCore core) {
    this.core = core;
  }

  public void initConfiguration() {
    // файл конфигурации должен лежать по пути: app/configuration.json
    var fileConfiguration = getPathToConfiguration().toFile();
    Configuration configuration;
    if (!fileConfiguration.exists()) {
      // создать новый по умолчанию
      configuration = Configuration.create();
      writeConfiguration(configuration, fileConfiguration);
    } else {
      // прочитать в текущие настройки
      configuration = Configuration.create(fileConfiguration);
    }
    core.setConfiguration(configuration);
  }

  public void writeConfiguration(Configuration configuration, File fileConfiguration) {
    // запишем ее в файл
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(fileConfiguration, configuration);
    } catch (IOException e) {
      LOGGER.error("Не удалось записать конфигурацию в файл.", e);
    }
  }

  public void writeConfiguration(Configuration configuration) {
    writeConfiguration(configuration, getPathToConfiguration().toFile());
  }

  private Path createPathToLog() {
    var path = Path.of(basePathApp.toString(), "logs").toAbsolutePath();
    path.toFile().mkdirs();
    return path;
  }

  private static Path createPathToConfiguration() {
    var path = Path.of(System.getProperty("user.home"), PhoenixCore.APPLICATION_NAME, "Configuration.json")
      .toAbsolutePath();
    path.getParent().toFile().mkdirs();
    return path;
  }
}

package com.github.otymko.phoenixbsl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class Configuration {

  private static final String DEFAULT_PATH_TO_BSL_LS = "app/bsl-language-server/bsl-language-server.exe";
  private static final boolean DEFAULT_USE_JAR_BSL_LS = false;
  private static final String DEFAULT_PATH_TO_JAVA = "java";
  private static final boolean DEFAULT_USE_CUSTOM_BSL_LS_CONFIGURATION = false;
  private static final String DEFAULT_PATH_TO_BSL_LS_CONFIGURATION = ".bsl-language-server.json";
  private static final boolean DEFAULT_USE_GROUP_ISSUES_BY_SEVERITY = false;

  private boolean usePathToJarBSLLS;
  private String pathToBSLLS;
  private String pathToJava;
  private boolean useCustomBSLLSConfiguration;
  private String pathToBSLLSConfiguration;
  private boolean useGroupIssuesBySeverity = DEFAULT_USE_GROUP_ISSUES_BY_SEVERITY;

  public Configuration() {
    setPathToBSLLS(DEFAULT_PATH_TO_BSL_LS);
    setUsePathToJarBSLLS(DEFAULT_USE_JAR_BSL_LS);
    setPathToJava(DEFAULT_PATH_TO_JAVA);
    setUseCustomBSLLSConfiguration(DEFAULT_USE_CUSTOM_BSL_LS_CONFIGURATION);
    setPathToBSLLSConfiguration(DEFAULT_PATH_TO_BSL_LS_CONFIGURATION);
    setUseGroupIssuesBySeverity(DEFAULT_USE_GROUP_ISSUES_BY_SEVERITY);
  }

  public static Configuration create(File configurationFile) {

    Configuration configuration = null;
    if (configurationFile.exists()) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(ACCEPT_CASE_INSENSITIVE_ENUMS);
      try {
        configuration = mapper.readValue(configurationFile, Configuration.class);
      } catch (IOException e) {
        LOGGER.error("Не удалось прочитать файл с конфигурацией приложения", e);
      }
    }

    if (configuration == null) {
      configuration = create();
    }
    return configuration;

  }

  public static Configuration create() {
    return new Configuration();
  }

}

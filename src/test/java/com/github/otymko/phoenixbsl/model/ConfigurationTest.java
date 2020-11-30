package com.github.otymko.phoenixbsl.model;

import com.github.otymko.phoenixbsl.logic.PhoenixContext;
import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationTest {

  @Test
  public void testCreate() {
    var configuration = Configuration.create();
    assertThat(configuration).isNotNull();
    assertThat(configuration.getPathToBSLLS())
      .isEqualTo(Configuration.DEFAULT_PATH_TO_BSL_LS);
    assertThat(configuration.isUsePathToJarBSLLS())
      .isEqualTo(Configuration.DEFAULT_USE_JAR_BSL_LS);
    assertThat(configuration.getPathToJava())
      .isEqualTo(Configuration.DEFAULT_PATH_TO_JAVA);
    assertThat(configuration.isUseCustomBSLLSConfiguration())
      .isEqualTo(Configuration.DEFAULT_USE_CUSTOM_BSL_LS_CONFIGURATION);
    assertThat(configuration.getPathToBSLLSConfiguration())
      .isEqualTo(PhoenixContext.BSL_CONFIGURATION_NAME);
    assertThat(configuration.isUseGroupIssuesBySeverity())
      .isEqualTo(Configuration.DEFAULT_USE_GROUP_ISSUES_BY_SEVERITY);
  }

  @Test
  public void testCreateFromFile() {

    Path path;
    Configuration configuration;

    path = Path.of("src/test/resources/Configuration.json");
    configuration = Configuration.create(path.toFile());
    assertThat(configuration).isNotNull();
    assertThat(configuration.getPathToBSLLS()).isEqualTo("app/bsl-language-server/bsl-language-server.exe");
    assertThat(configuration.isUsePathToJarBSLLS()).isFalse();
    assertThat(configuration.getPathToJava()).isEqualTo("java");
    assertThat(configuration.isUseCustomBSLLSConfiguration()).isFalse();
    assertThat(configuration.getPathToBSLLSConfiguration()).isEqualTo(".my-bsl-language-server.json");
    assertThat(configuration.isUseGroupIssuesBySeverity()).isTrue();

    path = Path.of("src/test/resources/Configuration");
    configuration = Configuration.create(path.toFile());
    assertThat(configuration).isNotNull();

    path = Path.of("src/test/resources/Blank.json");
    configuration = Configuration.create(path.toFile());
    assertThat(configuration).isNotNull();

  }

}
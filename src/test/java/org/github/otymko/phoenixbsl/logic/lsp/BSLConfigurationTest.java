package org.github.otymko.phoenixbsl.logic.lsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class BSLConfigurationTest {

  @Test
  public void test_OldConfiguration() {

    Path path = Path.of("src/test/resources/LSConfiguration/old.bsl-language-server.json");
    var configuration = getConfigurationFromFile(path.toFile());
    assertThat(configuration).isNotNull();

  }

  @Test
  public void test_Configuration() {

    Path path = Path.of("src/test/resources/LSConfiguration/.bsl-language-server.json");
    var configuration = getConfigurationFromFile(path.toFile());
    assertThat(configuration).isNotNull();
    assertThat(configuration.getLanguage()).isNotNull();

  }

  private BSLConfiguration getConfigurationFromFile(File file) {
    BSLConfiguration configuration = null;
    ObjectMapper mapper = new ObjectMapper();
    try {
      configuration = mapper.readValue(file, BSLConfiguration.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return configuration;
  }

}

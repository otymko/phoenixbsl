package org.github.otymko.phoenixbsl.core;

import lombok.Data;

@Data
public class ConfigurationApp {

  private boolean usePathToJarBSLLS = false;
  private String pathToBSLLS = "app/bsl-language-server/bsl-language-server.exe";

}

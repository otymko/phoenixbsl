package com.github.otymko.phoenixbsl.logic.sonarlint;

import lombok.extern.slf4j.Slf4j;
import org.sonarsource.sonarlint.core.client.api.common.LogOutput;

@Slf4j
public class CustomLogOutput implements LogOutput {
  @Override
  public void log(String formattedMessage, Level level) {
    if (level == Level.INFO || level == Level.DEBUG) {
      LOGGER.debug(formattedMessage);
    }
  }
}

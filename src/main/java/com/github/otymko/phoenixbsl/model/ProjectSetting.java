package com.github.otymko.phoenixbsl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.nio.file.Path;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ProjectSetting {
  private String name;
  private Path basePath;

  private boolean useSonarLint;
  private String projectKey;
  private String serverUrl;
  private String serverId;
  private String token;

  @JsonIgnore
  @Getter(lazy = true)
  private final Path fakePath = computeFakePath();

  @Override
  public String toString() {
    return name;
  }

  private Path computeFakePath() {
    return Path.of(basePath.toString(), "Module.bsl");
  }
}

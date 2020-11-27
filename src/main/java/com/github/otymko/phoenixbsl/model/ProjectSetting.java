package com.github.otymko.phoenixbsl.model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class ProjectSetting {
  private String projectKey;
  private Path basePath;
  private String serverUrl;
  private String serverId;
  private String token;
  private Path fakePath;
}

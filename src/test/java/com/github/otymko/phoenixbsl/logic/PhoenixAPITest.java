package com.github.otymko.phoenixbsl.logic;

import junit.framework.TestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoenixAPITest extends TestCase {

  @Test
  public void test_getProcessId() {

     var pid = PhoenixAPI.getProcessId();
     assertThat(pid).isGreaterThan(0);

  }

}
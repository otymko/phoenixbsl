package com.github.otymko.phoenixbsl.logic;

import org.junit.Test;

import java.awt.event.KeyEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomRobotTest {

  @Test
  public void test_getListKeyEventByNumber() {

    var value = 1023456789;
    var list = CustomRobot.getListKeyEventByNumber(value);
    assertThat(list.get(0)).isEqualTo(KeyEvent.VK_1);
    assertThat(list.get(1)).isEqualTo(KeyEvent.VK_0);
    assertThat(list.get(2)).isEqualTo(KeyEvent.VK_2);
    assertThat(list.get(3)).isEqualTo(KeyEvent.VK_3);
    assertThat(list.get(4)).isEqualTo(KeyEvent.VK_4);
    assertThat(list.get(5)).isEqualTo(KeyEvent.VK_5);
    assertThat(list.get(6)).isEqualTo(KeyEvent.VK_6);
    assertThat(list.get(7)).isEqualTo(KeyEvent.VK_7);
    assertThat(list.get(8)).isEqualTo(KeyEvent.VK_8);
    assertThat(list.get(9)).isEqualTo(KeyEvent.VK_9);

  }

}
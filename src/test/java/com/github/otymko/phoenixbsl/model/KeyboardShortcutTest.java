package com.github.otymko.phoenixbsl.model;

import junit.framework.TestCase;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyboardShortcutTest extends TestCase {

  @Test
  public void testConstructor() {
    var key = new KeyboardShortcut(GlobalKeyEvent.VK_I, true);
    assertThat(key.getKeyList()).hasSize(1);
    assertThat(key.isControlPressed()).isTrue();

    var keyList = new ArrayList<Integer>();
    keyList.add(GlobalKeyEvent.VK_I);
    keyList.add(GlobalKeyEvent.VK_K);

    var keyShortcut = new KeyboardShortcut(keyList, false);
    assertThat(keyShortcut.getKeyList()).hasSize(2);
    assertThat(keyShortcut.isControlPressed()).isFalse();
  }

  @Test
  public void testEquals() {
    List<KeyboardShortcut> listKey = new ArrayList<>();
    listKey.add(new KeyboardShortcut(GlobalKeyEvent.VK_I, true));
    listKey.add(new KeyboardShortcut(GlobalKeyEvent.VK_I, true));
    listKey.add(new KeyboardShortcut(GlobalKeyEvent.VK_J, true));

    assertThat(listKey.get(0).equals(listKey.get(0))).isTrue();
    assertThat(listKey.get(0).equals(listKey.get(1))).isTrue();
    assertThat(listKey.get(0).equals(listKey.get(2))).isFalse();
  }

}
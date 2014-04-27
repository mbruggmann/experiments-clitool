package ch.mbruggmann.clitool.impl;

import com.google.common.primitives.Primitives;

public class CommandArgument {

  private final String name;
  private final Class<?> type;

  public static CommandArgument of(String name, Class<?> type) {
    return new CommandArgument(name, Primitives.wrap(type));
  }

  private CommandArgument(String name, Class<?> type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }
}

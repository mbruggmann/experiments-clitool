package ch.mbruggmann.clitool;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CliToolTest {

  public static class Cli extends CliTool {

    private String name = null;
    private int length = -1;

    @Command
    public void get(@Argument("name") String n, @Argument("length") int l) {
      name = n;
      length = l;
    }

  };

  @Test
  public void testMixedArguments() {
    final String name = "name";
    final int length = 42;

    Cli cli = new Cli();
    CliTool.main(cli, "get", name, String.valueOf(length));

    assertEquals(name, cli.name);
    assertEquals(length, cli.length);
  }

}

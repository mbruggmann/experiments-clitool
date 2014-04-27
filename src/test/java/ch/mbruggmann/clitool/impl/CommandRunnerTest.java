package ch.mbruggmann.clitool.impl;

import ch.mbruggmann.clitool.Argument;
import ch.mbruggmann.clitool.CliTool;
import ch.mbruggmann.clitool.Command;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class CommandRunnerTest {

  @Test
  public void testNoArgs() {
    final AtomicBoolean called = new AtomicBoolean(false);
    CliTool cli = new CliTool() {
      @Command
      public void get() {
        called.set(true);
      }
    };

    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);
    runner.run(cli, mock(Namespace.class));
    assertTrue(called.get());
  }

  @Test
  public void testStringArgument() {
    final String argument = "s1";
    final String expected = "some-argument-string";

    final AtomicBoolean called = new AtomicBoolean(false);
    CliTool cli = new CliTool() {
      @Command
      public void get(@Argument(argument) String s1) {
        if (expected.equals(s1))
          called.set(true);
      }
    };

    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);
    Namespace namespace = mock(Namespace.class);
    when(namespace.get(argument)).thenReturn(expected);
    runner.run(cli, namespace);
    assertTrue(called.get());
  }

  @Test
  public void testMixedArguments() {
    final String name = "name";
    final UUID uuid = UUID.randomUUID();
    final int length = 42;

    final AtomicBoolean called = new AtomicBoolean(false);
    CliTool cli = new CliTool() {
      @Command
      public void get(@Argument("name") String n, @Argument("uuid") UUID u, @Argument("length") int l) {
        if (name.equals(n) && uuid.equals(u) && length == l)
          called.set(true);
      }
    };

    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);
    Namespace namespace = mock(Namespace.class);
    when(namespace.get("name")).thenReturn(name);
    when(namespace.get("uuid")).thenReturn(uuid);
    when(namespace.get("length")).thenReturn(length);
    runner.run(cli, namespace);
    assertTrue(called.get());
  }

  @Test
  public void testSubparser() {
    final String argumentName = "uuid";

    CliTool cli = new CliTool() {
      @Command
      public void get(@Argument(argumentName) UUID u) {
      }
    };

    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);

    net.sourceforge.argparse4j.inf.Argument argument = mock( net.sourceforge.argparse4j.inf.Argument.class);
    Subparser subparser = mock(Subparser.class);
    when(subparser.addArgument(eq(argumentName))).thenReturn(argument);
    Subparsers subparsers = mock(Subparsers.class);
    when(subparsers.addParser(eq("get"))).thenReturn(subparser);

    runner.addSubparser(subparsers);
    verify(subparser).addArgument(eq(argumentName));
    verify(argument).type(UUID.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotAnnotated() {
    CliTool cli = new CliTool() {
      public void get() {
      }
    };
    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArgumentNotAnnotated() {
    CliTool cli = new CliTool() {
      @Command
      public void get(String s1) {
      }
    };
    CommandRunner runner = CommandRunner.fromAnnotatedMethod(cli.getClass().getMethods()[0]);
  }

}

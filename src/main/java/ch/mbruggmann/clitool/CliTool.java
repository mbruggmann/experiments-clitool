package ch.mbruggmann.clitool;

import ch.mbruggmann.clitool.impl.CommandRunner;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract superclass for CLI tools.
 */
public abstract class CliTool {

  private static final String SUBPARSER_NAME = "subparser-name";

  public static void main(CliTool cli, String... args) {
    checkNotNull(cli);
    checkNotNull(args);

    // set up the argument parser
    final String programName = cli.getClass().getSimpleName();
    ArgumentParser parser = ArgumentParsers.newArgumentParser(programName).defaultHelp(true);
    cli.addGlobalOptions(parser);

    // create command runners based on the annotations in the cli object
    final Map<String, CommandRunner> commands = Maps.newHashMap();
    for (Method method : cli.getClass().getDeclaredMethods()) {
      Optional<Command> command = Optional.fromNullable(method.getAnnotation(Command.class));
      if (command.isPresent()) {
        CommandRunner commandRunner = CommandRunner.fromAnnotatedMethod(method);
        commands.put(commandRunner.getName(), commandRunner);
      }
    }

    // set up subparsers for all commands
    Subparsers subparsers = parser.addSubparsers().dest(SUBPARSER_NAME);
    for (CommandRunner command: commands.values()) {
      command.addSubparser(subparsers);
    }

    // parse the arguments
    final Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }

    // run the command
    cli.init(namespace);
    try {
      String subparser = namespace.getString(SUBPARSER_NAME);
      if (commands.containsKey(subparser)) {
        commands.get(subparser).run(cli, namespace);
      } else {
        System.err.println("no command found to run.");
      }
    } finally {
      cli.destroy();
    }
  }

  /**
   * Subclasses might override this method to define global arguments.
   *
   * @param parser the argument parser.
   */
  protected void addGlobalOptions(ArgumentParser parser) {
  }

  /**
   * Subclasses might override this method to set up state in the cli tool.
   *
   * This method is called exactly once, after parsing the arguments, and before calling the command.
   * @param namespace the parsed arguments.
   */
  protected void init(Namespace namespace) {
  }

  /**
   * Subclasses might override this method to tear down state in the cli tool.
   *
   * This method is called after the command has finished running, even if it threw an exception.
   */
  protected void destroy() {
  }

}

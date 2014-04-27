package ch.mbruggmann.clitool;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract superclass for CLI tools.
 */
public abstract class CliTool {

  private static final String SUBPARSER_NAME = "subparser-name";

  public static void main(CliTool cli, String[] args) {
    checkNotNull(cli);
    checkNotNull(args);

    final String programName = cli.getClass().getSimpleName();
    ArgumentParser parser = ArgumentParsers.newArgumentParser(programName).defaultHelp(true);

    cli.addGlobalOptions(parser);

    Subparsers subparsers = parser.addSubparsers().dest(SUBPARSER_NAME);
    final Map<String, CommandRunner> commands = Maps.newHashMap();
    for (Method method : cli.getClass().getDeclaredMethods()) {
      Optional<Command> command = Optional.fromNullable(method.getAnnotation(Command.class));
      if (command.isPresent()) {
        CommandRunner commandRunner = CommandRunner.fromAnnotatedMethod(method);
        commandRunner.addSubparser(subparsers);
        commands.put(commandRunner.getName(), commandRunner);
      }
    }


    final Namespace namespace;
    try {
      namespace = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return;
    }

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

  protected void addGlobalOptions(ArgumentParser parser) {
  }

  protected void init(Namespace namespace) {
  }

  protected void destroy() {
  }

  private static final class CommandRunner {

    private final String name;
    private final Argument[] arguments;
    private final Class<?>[] argumentTypes;
    private final Method method;

    public static CommandRunner fromAnnotatedMethod(Method method) {
      checkArgument(method != null);

      Command command = method.getAnnotation(Command.class);
      checkArgument(command != null, "method needs to have the command annotation");

      String name = method.getName();

      Class<?>[] argumentTypes = method.getParameterTypes();
      Argument[] arguments = new Argument[argumentTypes.length];
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i=0; i<argumentTypes.length; i++) {
        Annotation[] annotations = parameterAnnotations[i];
        checkArgument(
            annotations.length == 1 && annotations[0].annotationType() == Argument.class,
            "command parameters must have the argument annotation");
        arguments[i] = (Argument) annotations[0];
      }

      return new CommandRunner(name, arguments, argumentTypes, method);
    }

    private CommandRunner(String name, Argument[] arguments, Class<?>[] argumentTypes, Method method) {
      this.name = name;
      this.arguments = arguments;
      this.argumentTypes = argumentTypes;
      this.method = method;
    }

    public String getName() {
      return name;
    }

    public void addSubparser(Subparsers subparsers) {
      Subparser subparser = subparsers.addParser(getName());
      for (int i=0; i<arguments.length; i++) {
        subparser.addArgument(arguments[i].value()).type(Primitives.wrap(argumentTypes[i]));
      }
    }

    public void run(CliTool cli, Namespace namespace) {
      System.out.println("namespcae " + namespace);
      Object[] methodArguments = new Object[arguments.length];
      for (int i=0; i<arguments.length; i++) {
        methodArguments[i] = namespace.get(arguments[i].value());
      }

      try {
        method.invoke(cli, methodArguments);
      } catch (Exception e) {
        throw new RuntimeException("can't run command", e);
      }
    }
  }

}

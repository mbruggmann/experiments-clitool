package ch.mbruggmann.clitool.impl;

import ch.mbruggmann.clitool.Argument;
import ch.mbruggmann.clitool.CliTool;
import ch.mbruggmann.clitool.Command;
import com.google.common.collect.ImmutableList;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class CommandRunner {

  private final String name;
  private final List<CommandArgument> arguments;
  private final Method method;

  public static CommandRunner fromAnnotatedMethod(Method method) {
    checkArgument(method != null);

    Command command = method.getAnnotation(Command.class);
    checkArgument(command != null, "method needs to have the command annotation");

    String name = method.getName();

    ImmutableList.Builder<CommandArgument> arguments = ImmutableList.builder();
    Class<?>[] argumentTypes = method.getParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i=0; i<argumentTypes.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      checkArgument(
          annotations.length == 1 && annotations[0].annotationType() == Argument.class,
          "command parameters must have the argument annotation");
      arguments.add(CommandArgument.of(((Argument) annotations[0]).value(), argumentTypes[i]));
    }

    return new CommandRunner(name, arguments.build(), method);
  }

  private CommandRunner(String name, List<CommandArgument> arguments, Method method) {
    this.name = name;
    this.arguments = arguments;
    this.method = method;
  }

  public String getName() {
    return name;
  }

  public void addSubparser(Subparsers subparsers) {
    Subparser subparser = subparsers.addParser(name);
    for (CommandArgument argument : arguments) {
      subparser.addArgument(argument.getName()).type(argument.getType());
    }
  }

  public void run(CliTool cli, Namespace namespace) {
    System.out.println("namespcae " + namespace);
    Object[] methodArguments = new Object[arguments.size()];
    for (int i=0; i<arguments.size(); i++) {
      methodArguments[i] = namespace.get(arguments.get(i).getName());
    }

    try {
      method.invoke(cli, methodArguments);
    } catch (Exception e) {
      throw new RuntimeException("can't run command", e);
    }
  }
}

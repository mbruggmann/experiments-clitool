package ch.mbruggmann.clitool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes an argument to a CLI command.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {

  /**
   * The name of the argument, used to identify this specific argument in the help text.
   */
  String value();
}

package ch.mbruggmann.clitool;

@Program(name = "demo", description = "demo how to use the CliTool")
public class Demo extends CliTool {

  @Command
  public void get(@Argument("arg1") String arg1, @Argument("arg2") int arg2) {
    System.out.println(arg1);
    System.out.println(arg2);
  }

  public static void main(String[] args) throws Exception {
    CliTool.main(new Demo(), args);
  }

}

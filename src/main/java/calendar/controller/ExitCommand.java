package calendar.controller;

import calendar.model.IcalendarManager;

/**
 * Exit command that cleanly signals the parser to stop.
 */
public class ExitCommand extends AbstractCommand {

  /**
   * A final, static unchecked exception used solely as an internal **signal**
   * to stop the application's main execution loop and trigger a graceful **exit**.
   */
  public static final class ExitSignal extends RuntimeException {

    /**
     * Constructs an {@code ExitSignal} with the internal message "EXIT-SIGNAL".
     * The exception itself, not its message, is used to signal termination.
     */
    public ExitSignal() {
      super("EXIT-SIGNAL");
    }
  }

  /**
   * Constructs the exit command.
   * It is initialized with {@code null} arguments because this command takes no parameters.
   */
  public ExitCommand() {
    super(null);
  }

  @Override
  public void execute(IcalendarManager manager) {
    // Print user-friendly message
    System.out.println("exiting...");
    // Stop parser loop
    throw new ExitSignal();
  }
}

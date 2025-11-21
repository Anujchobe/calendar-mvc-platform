package calendar.controller;

import calendar.model.IcalendarManager;
import java.util.List;

/**
 * A skeletal base class for all command implementations.
 *
 * <p>Provides common utilities such as argument validation,
 * and stores the raw tokenized command arguments.</p>
 */
public abstract class AbstractCommand implements Command {

  /** The raw tokenized arguments of the command. */
  protected final List<String> args;

  /**
   * Constructs an {@code AbstractCommand} with the given argument list.
   *
   * @param args The raw tokenized command arguments.
   */
  protected AbstractCommand(List<String> args) {
    this.args = args;
  }

  /**
   * Executes this command using the provided calendar manager.
   *
   * <p>Concrete subclasses must implement this method to define
   * specific command behavior.</p>
   *
   * @param manager The calendar manager to operate on.
   */
  @Override
  public abstract void execute(IcalendarManager manager);

  /**
   * Ensures that the command has at least the given number of arguments.
   *
   * @param count minimum number of expected arguments
   * @param usage usage message to show in case of invalid arguments
   * @throws IllegalArgumentException if there are fewer than {@code count} arguments
   */
  protected void ensureArgCountAtLeast(int count, String usage) {
    if (args == null || args.size() < count) {
      throw new IllegalArgumentException("Invalid arguments. " + usage);
    }
  }
}

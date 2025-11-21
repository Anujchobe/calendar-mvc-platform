package calendar.controller;

import calendar.model.IcalendarManager;
import java.util.List;

/**
 * Command to switch the active calendar.
 * Usage: use calendar --name calName
 */
public class UseCalendarCommand extends AbstractCommand {

  /**
   * Constructs the use calendar command with the provided arguments.
   *
   * @param args The list of string arguments, typically specifying the name
   *             of the calendar to be selected or switched to.
   */
  public UseCalendarCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    ensureArgCountAtLeast(3, "Usage: use calendar --name <calName>");

    String name = null;
    for (int i = 0; i < args.size(); i++) {
      if ("--name".equalsIgnoreCase(args.get(i)) && i + 1 < args.size()) {
        name = args.get(++i);
      }
    }

    if (name == null) {
      throw new IllegalArgumentException("Missing required argument: --name <calName>");
    }

    manager.useCalendar(name);
    System.out.println("Now using calendar: " + name);
  }
}

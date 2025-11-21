package calendar.controller;

import calendar.model.IcalendarManager;
import java.util.List;

/**
 * Command to edit calendar-level properties such as name or timezone.
 * Usage: edit calendar --name calName --property property newValue
 */
public class EditCalendarCommand extends AbstractCommand {

  /**
   * Constructs the edit calendar command with the provided arguments.
   *
   * @param args The list of string arguments, typically specifying the calendar
   *             to be edited and the new attribute values.
   */
  public EditCalendarCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    ensureArgCountAtLeast(6,
        "Usage: edit calendar --name <calName> --property <property> <newValue>");

    String name = null;
    String property = null;
    String newValue = null;

    for (int i = 0; i < args.size(); i++) {
      switch (args.get(i).toLowerCase()) {
        case "--name":
          name = args.get(++i);
          break;
        case "--property":
          property = args.get(++i);
          if (i + 1 < args.size()) {
            newValue = args.get(++i);
          }
          break;
        default:
          break;
      }
    }

    if (name == null || property == null || newValue == null) {
      throw new IllegalArgumentException(
          "Missing arguments. Expected --name, --property, and value.");
    }

    manager.editCalendar(name, property, newValue);
    System.out.println("Calendar '" + name + "' updated: " + property + " → " + newValue);
  }
}

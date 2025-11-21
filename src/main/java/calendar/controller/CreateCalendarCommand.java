package calendar.controller;

import calendar.model.IcalendarManager;
import java.time.ZoneId;
import java.util.List;

/**
 * Command to create a new calendar with a unique name and timezone.
 * Usage: create calendar --name calName --timezone area,location
 */
public class CreateCalendarCommand extends AbstractCommand {

  /**
   * Constructs the create calendar command with the provided arguments.
   *
   * @param args The list of string arguments, typically specifying the new calendar's name.
   */
  public CreateCalendarCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    ensureArgCountAtLeast(5, "Usage: create calendar --name <calName> --timezone <area/location>");

    String name = null;
    String tzString = null;

    for (int i = 0; i < args.size(); i++) {
      switch (args.get(i).toLowerCase()) {
        case "--name":
          name = args.get(++i);
          break;
        case "--timezone":
          tzString = args.get(++i);
          break;
        default:
          break;
      }
    }

    if (name == null || tzString == null) {
      throw new IllegalArgumentException("Missing required fields: --name and --timezone");
    }

    ZoneId zone;
    try {
      zone = ZoneId.of(tzString);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone format: " + tzString);
    }

    manager.createCalendar(name, zone);
    System.out.println("Calendar created: " + name + " [" + zone + "]");
  }
}

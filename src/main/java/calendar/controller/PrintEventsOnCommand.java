package calendar.controller;

import calendar.model.Event;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.LocalDate;
import java.util.List;

/**
 * Command to print all calendar events on a specific date.
 */
public class PrintEventsOnCommand extends AbstractCommand {

  /**
   * Constructs the command to print events on a specific date.
   *
   * @param args The list of string arguments, expected to contain the target date.
   */
  public PrintEventsOnCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Manager cannot be null.");
    }

    Icalendar model = manager.getActiveCalendar();
    if (model == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar <name>' first.");
    }

    if (args.size() != 1) {
      throw new IllegalArgumentException("Usage: print events on <date>");
    }

    LocalDate date = ParseUtils.parseDate(args.get(0));
    List<Event> events = model.queryEventsOn(date);

    if (events.isEmpty()) {
      System.out.println("(no events found)");
      return;
    }

    System.out.printf("Events on %s:%n", date);
    for (Event e : events) {
      String loc = (e.getLocation() == null || e.getLocation().isBlank())
          ? ""
          : " (" + e.getLocation() + ")";
      System.out.printf(
          "- %s [%s → %s]%s%n",
          e.getSubject(),
          e.getStart().toLocalTime(),
          e.getEnd().toLocalTime(),
          loc
      );
    }
  }
}

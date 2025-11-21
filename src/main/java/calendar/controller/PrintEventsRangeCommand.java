package calendar.controller;

import calendar.model.Event;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to print all events between two date-time values.
 */
public class PrintEventsRangeCommand extends AbstractCommand {

  /**
   * Constructs the print events range command with the provided arguments.
   *
   * @param args The list of string arguments, expected to contain the start and end
   *             date-times defining the range.
   */
  public PrintEventsRangeCommand(List<String> args) {
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

    if (args.size() != 2) {
      throw new IllegalArgumentException("Usage: print events from <start> to <end>");
    }

    ZonedDateTime start = ParseUtils.parseDateTimeEst(args.get(0));
    ZonedDateTime end = ParseUtils.parseDateTimeEst(args.get(1));

    List<Event> events = model.queryEventsBetween(start, end);
    if (events.isEmpty()) {
      System.out.println("(no events found)");
      return;
    }

    System.out.printf("Events between %s and %s:%n",
        start.toLocalDate(), end.toLocalDate());

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

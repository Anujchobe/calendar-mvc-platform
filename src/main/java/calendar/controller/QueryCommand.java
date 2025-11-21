package calendar.controller;

import calendar.model.Event;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to query events by date or time range in the active calendar.
 *
 * <p>Supported usage:</p>
 * <ul>
 *   <li>{@code query date <YYYY-MM-DD>}</li>
 *   <li>{@code query range <start-ISO> <end-ISO>}</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <pre>
 * query date 2025-11-09
 * query range 2025-11-09T09:00 2025-11-09T17:00
 * </pre>
 */
public class QueryCommand extends AbstractCommand {

  /**
   * Constructs a QueryCommand with tokenized arguments.
   *
   * @param args A list of tokens specifying the query type and parameters.
   */
  public QueryCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null.");
    }

    Icalendar model = manager.getActiveCalendar();
    if (model == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar --name <name>' first.");
    }

    if (args == null || args.size() < 2) {
      throw new IllegalArgumentException(
          "Usage: query date <YYYY-MM-DD> | query range <start> <end>");
    }

    String type = args.get(0).toLowerCase();

    try {
      switch (type) {
        case "date":
          {
          LocalDate date = ParseUtils.parseDate(args.get(1));
          List<Event> events = model.queryEventsOn(date);
          printEvents(events);
          break;
          }

        case "range":
          {
          if (args.size() < 3) {
            throw new IllegalArgumentException(
                "Range query requires start and end date-times.");
          }
          ZonedDateTime start = ParseUtils.parseDateTimeEst(args.get(1));
          ZonedDateTime end = ParseUtils.parseDateTimeEst(args.get(2));
          List<Event> events = model.queryEventsBetween(start, end);
          printEvents(events);
          break;
          }

        default:
          throw new IllegalArgumentException("Unknown query type: " + args.get(0));
      }

    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      System.err.println("Query failed: " + e.getMessage());
    }
  }

  /**
   * Helper to print queried events in a readable format.
   */
  private void printEvents(List<Event> list) {
    if (list == null || list.isEmpty()) {
      System.out.println("(no events found)");
    } else {
      for (Event e : list) {
        String loc = e.getLocation() == null ? "" : " (" + e.getLocation() + ")";
        System.out.printf(
            "- %s on %s at %s–%s%s%n",
            e.getSubject(),
            e.getStart().toLocalDate(),
            e.getStart().toLocalTime(),
            e.getEnd().toLocalTime(),
            loc
        );
      }
    }
  }
}

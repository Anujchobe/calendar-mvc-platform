package calendar.controller;

import calendar.model.EventCopierFactory;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import calendar.model.IeventCopier;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Command to copy events within or between calendars.
 *
 * <p>Spec-compliant command formats:</p>
 * <ul>
 * <li>copy event &lt;eventName&gt; on &lt;dateTime&gt;
 * --target &lt;calendarName&gt; to &lt;dateTime&gt;</li>
 * <li>copy events on &lt;date&gt; --target &lt;
 * calendarName&gt; to &lt;date&gt;</li>
 * <li>copy events between &lt;date1&gt; and &lt;date2&gt; --target &lt;
 * calendarName&gt; to &lt;date&gt;</li>
 * </ul>
 */
public class CopyCommand extends AbstractCommand {

  /**
   * Constructs the copy command with the provided arguments.
   *
   * @param args The list of string arguments, typically specifying the source
   *             and destination events or periods.
   */
  public CopyCommand(List<String> args) {
    super(args);
  }

  /**
   * Executes the copy command, validating the input and delegating to the appropriate.
   * copy method based on the command type ("event" or "events").
   *
   * @param manager The calendar manager containing all available calendars.
   * @throws IllegalArgumentException if syntax is invalid or a calendar is missing.
   * @throws IllegalStateException    if no calendar is currently active.
   */
  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Manager cannot be null.");
    }

    ensureArgCountAtLeast(5, "Usage: copy event|events ...");


    final String type = args.get(1).toLowerCase();

    Icalendar sourceCal = manager.getActiveCalendar();
    if (sourceCal == null) {
      throw new IllegalStateException("No active calendar selected.");
    }

    int targetIdx = args.indexOf("--target");
    if (targetIdx == -1 || targetIdx + 1 >= args.size()) {
      throw new IllegalArgumentException("Missing target calendar. Use --target <calendarName>");
    }
    String targetName = args.get(targetIdx + 1);
    Icalendar targetCal = manager.getCalendar(targetName);
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar '" + targetName + "' not found.");
    }

    IeventCopier copier = EventCopierFactory.createCopier(sourceCal);

    switch (type) {
      case "event":
        copySingleEvent(copier, targetCal, targetName);
        break;
      case "events":
        handleEventsCopy(copier, targetCal, targetName);
        break;
      default:

        throw new IllegalArgumentException(
            "Invalid syntax. Expected: copy event|events ...");
    }
  }

  /**
   * Handles {@code copy event <name> on <dateTime> ...}.
   *
   * @param copier    The event copier instance.
   * @param targetCal The target calendar model.
   */
  private void copySingleEvent(IeventCopier copier, Icalendar targetCal, String targetName) {
    String eventName = ParseUtils.parsePossiblyQuotedSubject(args, 2);
    int onIdx = args.indexOf("on");
    int toIdx = args.indexOf("to");

    if (onIdx == -1 || toIdx == -1 || onIdx + 1 >= args.size() || toIdx + 1 >= args.size()) {
      throw new IllegalArgumentException(
          "Usage: copy event <eventName> "
              + "on <startDateTime> --target <calendar> to <targetDateTime>");
    }

    try {
      ZonedDateTime sourceStart = ParseUtils.parseDateTimeEst(args.get(onIdx + 1));
      ZonedDateTime targetStart = ParseUtils.parseDateTimeEst(args.get(toIdx + 1));
      copier.copyEvent(eventName, sourceStart, targetCal, targetStart);
      System.out.printf("Copied event '%s' to calendar '%s' at %s%n",
          eventName, targetName, targetStart);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + e.getMessage());
    }
  }


  /**
   * Handles both {@code copy events on ...} and {@code copy events between ... and ...}.
   *
   * @param copier    The event copier instance.
   * @param targetCal The target calendar model.
   */
  private void handleEventsCopy(IeventCopier copier, Icalendar targetCal, String targetName) {
    if (args.contains("between")) {
      copyEventsInRange(copier, targetCal, targetName);
    } else if (args.contains("on")) {
      copyEventsByDate(copier, targetCal, targetName);
    } else {
      throw new IllegalArgumentException(
          "Usage: copy events on <date> ... | copy events between <start> and <end> ...");
    }
  }

  /**
   * Handles {@code copy events on <date> --target <calendar> to <date>}.
   *
   * @param copier    The event copier instance.
   * @param targetCal The target calendar model.
   */
  private void copyEventsByDate(IeventCopier copier, Icalendar targetCal, String targetName) {
    int onIdx = args.indexOf("on");
    int toIdx = args.indexOf("to");

    if (onIdx == -1 || toIdx == -1 || onIdx + 1 >= args.size() || toIdx + 1 >= args.size()) {
      throw new IllegalArgumentException(
          "Usage: copy events on <sourceDate> --target <calendar> to <targetDate>");
    }

    LocalDate sourceDate = ParseUtils.parseDate(args.get(onIdx + 1));
    LocalDate targetDate = ParseUtils.parseDate(args.get(toIdx + 1));
    copier.copyEventsOnDate(sourceDate, targetCal, targetDate);
    System.out.printf("Copied all events from %s to %s in calendar '%s'%n",
        sourceDate, targetDate, targetName);
  }

  /**
   * Handles {@code copy events between <date1> and <date2> --target <calendar> to <date>}.
   *
   * @param copier    The event copier instance.
   * @param targetCal The target calendar model.
   */
  private void copyEventsInRange(IeventCopier copier, Icalendar targetCal, String targetName) {
    int betweenIdx = args.indexOf("between");
    int andIdx = args.indexOf("and");
    int toIdx = args.lastIndexOf("to");

    if (betweenIdx == -1 || andIdx == -1 || toIdx == -1) {
      throw new IllegalArgumentException(
          "Usage: copy events between <start> and <end> --target <calendar> to <targetDate>");
    }

    LocalDate start = ParseUtils.parseDate(args.get(betweenIdx + 1));
    LocalDate end = ParseUtils.parseDate(args.get(andIdx + 1));
    LocalDate targetStart = ParseUtils.parseDate(args.get(toIdx + 1));

    copier.copyEventsBetween(start, end, targetCal, targetStart);
    System.out.printf("Copied events from %s–%s to start at %s in calendar '%s'%n",
        start, end, targetStart, targetName);
  }
}
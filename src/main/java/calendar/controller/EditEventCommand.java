package calendar.controller;

import calendar.model.EditMode;
import calendar.model.EventKey;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Command to edit one or more calendar events, including recurring series.
 *
 * <p>Supported syntax examples:</p>
 * <ul>
 * <li>{@code edit event "Team Meeting" from 2025-11-10T09:00 to
 * 2025-11-10T10:00 with location "Zoom"}</li>
 * <li>{@code edit events "Team Meeting" from 2025-11-10T09:00 with description
 * "Updated agenda"}</li>
 * <li>{@code edit series "Team Meeting" from 2025-11-10T09:00 with status PUBLIC}</li>
 * </ul>
 *
 * <p>All date/time input is assumed to be in Eastern Time.</p>
 */
public class EditEventCommand extends AbstractCommand {

  private final String scope;

  /**
   * Constructs the edit event command, initializing it with tokens and setting the edit scope.
   *
   * @param tokens The list of string tokens representing the command and its arguments.
   * @param scope  The scope of the edit (e.g., "single", "all future") for recurring events,
   *               defaulting to "event" if {@code null}.
   */
  public EditEventCommand(List<String> tokens, String scope) {
    super(tokens);
    this.scope = scope == null ? "event" : scope.toLowerCase();
  }

  /**
   * Executes the edit command, parsing the arguments and applying the change to active calendar.
   *
   * @param manager The calendar manager containing the active calendar model.
   * @throws IllegalArgumentException if syntax is invalid, required keywords are missing,
   *                                  or date/time format is wrong.
   * @throws IllegalStateException    if no calendar is currently active.
   */
  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Manager cannot be null.");
    }

    Icalendar model = manager.getActiveCalendar();
    if (model == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar --name <name>' first.");
    }

    if (args.size() < 6) {
      throw new IllegalArgumentException(
          "Invalid edit syntax. "
              + "Use: edit event|series <subject> from <start> [to <end>] with <property> <value>");
    }

    final String subject = args.get(2);

    int fromIdx = args.indexOf("from");
    if (fromIdx == -1 || fromIdx + 1 >= args.size()) {
      throw new IllegalArgumentException("Missing 'from' keyword.");
    }


    final ZonedDateTime start = safeParseDateTime(args.get(fromIdx + 1));
    ZonedDateTime end = null;

    if (fromIdx + 2 < args.size() && args.get(fromIdx + 2).equalsIgnoreCase("to")) {
      if (fromIdx + 3 >= args.size()) {
        throw new IllegalArgumentException("Missing end time after 'to'.");
      }
      end = safeParseDateTime(args.get(fromIdx + 3));
    }

    int withIdx = args.indexOf("with");
    if (withIdx == -1 || withIdx + 2 > args.size()) {
      throw new IllegalArgumentException("Missing 'with <property> <value>' section.");
    }

    String property = args.get(withIdx + 1).toLowerCase();
    StringBuilder valueBuilder = new StringBuilder();
    for (int i = withIdx + 2; i < args.size(); i++) {
      valueBuilder.append(args.get(i));
      if (i < args.size() - 1) {
        valueBuilder.append(" ");
      }
    }
    String newRaw = valueBuilder.toString().trim();

    Object newValue = coerceNewValue(property, newRaw);
    applyEdit(model, subject, start, end, property, newValue);
  }

  private void applyEdit(Icalendar model, String subject, ZonedDateTime start, ZonedDateTime end,
                         String property, Object newValue) {

    if (scope.equals("event")) {
      EventKey key = new EventKey(subject, start, end);
      model.editEvent(key, property, newValue);
      System.out.println(
          "Edited single event: " + subject
              +
              " (property = " + property
              +
              ", newValue = " + formatValue(newValue) + ")"
      );

    } else if (scope.equals("events")) {
      EventKey key = new EventKey(subject, start, null);
      model.editSeries(key, property, newValue, EditMode.FROM_THIS_ONWARD);
      System.out.println(
          "Edited events from this onward: " + subject
              +
              " (property = " + property + ", newValue = "
              + formatValue(newValue) + ")"
      );


    } else if (scope.equals("series")) {
      EventKey key = new EventKey(subject, start, null);
      model.editSeries(key, property, newValue, EditMode.ENTIRE_SERIES);
      System.out.println(
          "Edited entire series: " + subject
              +
              " (property = " + property
              +
              ", newValue = " + formatValue(newValue) + ")"
      );
    } else {
      throw new IllegalArgumentException("Invalid scope: " + scope);
    }
  }

  private Object coerceNewValue(String property, String raw) {
    switch (property) {
      case "start":
      case "end":
        return safeParseDateTime(raw);
      case "status":
        return raw.trim().toUpperCase();
      case "description":
      case "location":
      case "subject":
        return raw.trim();
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  private ZonedDateTime safeParseDateTime(String text) {
    try {
      return ParseUtils.parseDateTimeEst(text);
    } catch (DateTimeParseException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + text);
    }
  }

  /**
   * Formats a newValue for CLI output so that GUI and CLI output look clean.
   */
  private String formatValue(Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof ZonedDateTime) {
      return ((ZonedDateTime) value).toString();
    }
    return value.toString();
  }

}
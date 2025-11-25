package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import calendar.model.RecurrenceRule;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * Command to create calendar events (single or recurring) using
 * the {@link Event.Builder} pattern.
 *
 * <p>Supports input forms:
 * <ul>
 * <li>{@code create event "Meeting" from 2025-11-08T09:00 to 2025-11-08T10:00}</li>
 * <li>{@code create event "Yoga" on 2025-11-08 repeats MTWRF for 5}</li>
 * <li>Optionally add: {@code description "Weekly sync" location "Zoom" status PUBLIC}</li>
 * </ul>
 *
 * <p>All date/time input is assumed to be in Eastern Time.</p>
 */
public class CreateEventCommand extends AbstractCommand {

  private final List<String> tokens;

  /**
   * Constructs a new event creation command, initialized with the raw input tokens.
   *
   * @param tokens The list of string tokens representing the command and its arguments.
   */
  public CreateEventCommand(List<String> tokens) {
    super(tokens);
    this.tokens = tokens;
  }

  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null.");
    }

    Icalendar model = manager.getActiveCalendar();
    if (model == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar <name>' first.");
    }

    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Invalid create command syntax.");
    }

    try {
      if (!tokens.get(0).equalsIgnoreCase("create")
          || !tokens.get(1).equalsIgnoreCase("event")) {
        throw new IllegalArgumentException("Command must start with 'create event'.");
      }

      String subject = parseSubject(tokens, 2);

      if (tokens.contains("from")) {
        handleFromToVariant(model, subject);
      } else if (tokens.contains("on")) {
        handleOnVariant(model, subject);
      } else {
        throw new IllegalArgumentException("Missing 'from' or 'on' keyword.");
      }

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid create command syntax: " + e.getMessage());
    }
  }

  /**
   * Handles "create event ... from start to end" form.
   */
  private void handleFromToVariant(Icalendar model, String subject) {
    int fromIndex = tokens.indexOf("from");
    int toIndex = tokens.indexOf("to");

    if (fromIndex < 0 || toIndex <= fromIndex + 1 || toIndex + 1 >= tokens.size()) {
      throw new IllegalArgumentException("Missing 'from/to' arguments or end time.");
    }

    ZonedDateTime start = safeParseDateTime(tokens.get(fromIndex + 1));
    ZonedDateTime end = safeParseDateTime(tokens.get(toIndex + 1));

    Event.Builder builder = new Event.Builder(subject, start, end);

    parseOptionalFields(builder, toIndex + 2);

    Event event = builder.build();

    if (tokens.contains("repeats")) {
      handleRecurrence(model, event);
    } else {
      model.createEvent(event);
      System.out.println("Created single event: " + subject);
    }
  }

  /**
   * Handles "create event ... on date" form (all-day event 8 AM – 5 PM).
   */
  private void handleOnVariant(Icalendar model, String subject) {
    int onIndex = tokens.indexOf("on");
    if (onIndex < 0 || onIndex + 1 >= tokens.size()) {
      throw new IllegalArgumentException("Missing date after 'on'.");
    }

    LocalDate date = safeParseDate(tokens.get(onIndex + 1));
    ZoneId est = ZoneId.of("America/New_York");
    ZonedDateTime start = date.atTime(8, 0).atZone(est);
    ZonedDateTime end = date.atTime(17, 0).atZone(est);

    Event.Builder builder = new Event.Builder(subject, start, end).allDay(true);

    parseOptionalFields(builder, onIndex + 2);

    Event event = builder.build();

    if (tokens.contains("repeats")) {
      handleRecurrence(model, event);
    } else {
      model.createEvent(event);
      System.out.println("Created all-day event: " + subject);
    }
  }

  /**
   * Handles recurring event creation using the 'repeats' keyword.
   */
  private void handleRecurrence(Icalendar model, Event baseEvent) {
    int repeatsIndex = tokens.indexOf("repeats");
    if (repeatsIndex < 0 || repeatsIndex + 1 >= tokens.size()) {
      throw new IllegalArgumentException("Missing weekday pattern after 'repeats'.");
    }

    String pattern = tokens.get(repeatsIndex + 1).toUpperCase(Locale.ROOT);
    Set<DayOfWeek> days = parseWeekdays(pattern);

    Integer occurrences = null;
    LocalDate until = null;

    if (tokens.contains("for")) {
      int forIndex = tokens.indexOf("for");
      if (forIndex + 1 >= tokens.size()) {
        throw new IllegalArgumentException("Missing number after 'for'.");
      }
      occurrences = Integer.parseInt(tokens.get(forIndex + 1));
    } else if (tokens.contains("until")) {
      int untilIndex = tokens.indexOf("until");
      if (untilIndex + 1 >= tokens.size()) {
        throw new IllegalArgumentException("Missing date after 'until'.");
      }
      until = safeParseDate(tokens.get(untilIndex + 1));
    } else {
      throw new IllegalArgumentException("Expected 'for <N>' or 'until <date>' after repeats.");
    }

    RecurrenceRule rule = new RecurrenceRule(days, occurrences, until);
    model.createSeries(baseEvent, rule);

    System.out.printf("Created recurring event: %s (%s)%n",
        baseEvent.getSubject(),
        occurrences != null ? occurrences + " times" : "until " + until);
  }


  private void parseOptionalFields(Event.Builder builder, int startIndex) {
    for (int i = startIndex; i < tokens.size(); i++) {
      String t = tokens.get(i).toLowerCase(Locale.ROOT);

      if (t.equals("description") && i + 1 < tokens.size()) {
        builder.description(ParseUtils.parsePossiblyQuotedSubject(tokens, i + 1));
      }

      if (t.equals("location") && i + 1 < tokens.size()) {
        builder.location(ParseUtils.parsePossiblyQuotedSubject(tokens, i + 1));
      }

      if (t.equals("status") && i + 1 < tokens.size()) {
        try {
          builder.status(EventStatus.valueOf(tokens.get(i + 1).toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid status. Use PUBLIC or PRIVATE.");
        }
      }
    }
  }

  private Set<DayOfWeek> parseWeekdays(String input) {
    Map<Character, DayOfWeek> map = Map.of(
        'M', DayOfWeek.MONDAY,
        'T', DayOfWeek.TUESDAY,
        'W', DayOfWeek.WEDNESDAY,
        'R', DayOfWeek.THURSDAY,
        'F', DayOfWeek.FRIDAY,
        'S', DayOfWeek.SATURDAY,
        'U', DayOfWeek.SUNDAY
    );
    Set<DayOfWeek> result = new HashSet<>();
    for (char c : input.toCharArray()) {
      DayOfWeek d = map.get(c);
      if (d == null) {
        throw new IllegalArgumentException("Invalid weekday: " + c);
      }
      result.add(d);
    }
    return result;
  }

  private ZonedDateTime safeParseDateTime(String text) {
    try {
      return ParseUtils.parseDateTimeEst(text);
    } catch (DateTimeParseException | IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + text);
    }
  }

  private LocalDate safeParseDate(String text) {
    try {
      return LocalDate.parse(text);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + text);
    }
  }

  private String parseSubject(List<String> tokens, int startIndex) {
    String first = tokens.get(startIndex);
    if (first.startsWith("\"")) {
      StringBuilder sb = new StringBuilder();
      for (int i = startIndex; i < tokens.size(); i++) {
        sb.append(tokens.get(i)).append(" ");
        if (tokens.get(i).endsWith("\"")) {
          String full = sb.toString().trim();
          return full.substring(1, full.length() - 1).trim();
        }
      }
      throw new IllegalArgumentException("Unclosed quoted subject.");
    }
    return first;
  }
}
package calendar.controller;

import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Implementation of IcommandAdapter that converts GUI parameters
 * into CLI-style command strings compatible with the existing
 * CommandParser / CommandFactory pipeline.
 */
public class CommandAdapter implements IcommandAdapter {

  // -------------------------------------------------------
  // Helper methods
  // -------------------------------------------------------

  /** Wraps a string in quotes, escaping inner quotes. */
  private String quote(String s) {
    if (s == null || s.isEmpty()) {
      return "\"\"";
    }
    return "\"" + s.replace("\"", "\\\"") + "\"";
  }

  /**
   * Formats a ZonedDateTime as a local ISO-8601 datetime string in EST,
   * with no zone/offset (e.g., "2025-11-24T11:05").
   */
  private String formatZdtLocalIso(ZonedDateTime zdt) {
    if (zdt == null) {
      return "";
    }
    LocalDateTime ldt = zdt.withZoneSameInstant(ParseUtils.EST).toLocalDateTime();
    return ldt.toString(); // "yyyy-MM-ddTHH:mm[:ss]"
  }

  /** Formats a LocalDate as "yyyy-MM-dd". */
  private String formatDate(LocalDate date) {
    return date == null ? "" : date.toString();
  }

  /**
   * Formats the "newValue" part for an edit command, taking into account
   * special handling for start/end (date-times) and status.
   */
  private String formatPropertyValue(String property, Object newValue) {
    if (newValue == null) {
      return "\"\"";
    }

    String prop = property.toLowerCase();

    // For start/end, ensure we send a plain ISO local datetime string
    if ("start".equals(prop) || "end".equals(prop)) {
      if (newValue instanceof ZonedDateTime) {
        String iso = formatZdtLocalIso((ZonedDateTime) newValue);
        return quote(iso); // e.g. "2025-11-24T11:05"
      } else if (newValue instanceof LocalDateTime) {
        return quote(((LocalDateTime) newValue).toString());
      } else {
        // Fallback – still quote so tokenizer keeps it together
        return quote(String.valueOf(newValue));
      }
    }

    // Status: PUBLIC / PRIVATE (EditEventCommand uppercases it again)
    if ("status".equals(prop)) {
      return quote(String.valueOf(newValue).toUpperCase());
    }

    // All other properties (subject/location/description) are plain text
    return quote(String.valueOf(newValue));
  }

  // -------------------------------------------------------
  // Calendar commands
  // -------------------------------------------------------

  @Override
  public String buildCreateCalendarCommand(String name, String timezone) {
    return "create calendar "
        + "--name " + quote(name) + " "
        + "--timezone " + quote(timezone);
  }

  @Override
  public String buildUseCalendarCommand(String calendarName) {
    return "use calendar --name " + quote(calendarName);
  }

  // -------------------------------------------------------
  // Event creation (single)
  // -------------------------------------------------------

  @Override
  public String buildCreateSingleEventCommand(String subject,
                                              ZonedDateTime start,
                                              ZonedDateTime end,
                                              String description,
                                              String location,
                                              EventStatus status) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);

    StringBuilder sb = new StringBuilder();

    // Matches CreateEventCommand "from/to" variant
    sb.append("create event ")
        .append(quote(subject)).append(" ")
        .append("from ").append(startStr).append(" ")
        .append("to ").append(endStr).append(" ");

    // Optional fields parsed by CreateEventCommand.parseOptionalFields
    if (description != null && !description.isEmpty()) {
      sb.append("description ").append(quote(description)).append(" ");
    }

    if (location != null && !location.isEmpty()) {
      sb.append("location ").append(quote(location)).append(" ");
    }

    // status PUBLIC/PRIVATE
    if (status != null) {
      sb.append("status ").append(status.name());
    }

    return sb.toString().trim();
  }

  // -------------------------------------------------------
  // Event creation (recurring)
  // -------------------------------------------------------

  @Override
  public String buildCreateRecurringEventCommand(String subject,
                                                 ZonedDateTime start,
                                                 ZonedDateTime end,
                                                 String description,
                                                 String location,
                                                 EventStatus status,
                                                 String weekdayPattern,
                                                 Integer occurrences,
                                                 LocalDate endDate) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);

    StringBuilder sb = new StringBuilder();

    // Base "create event ... from ... to ..." form
    sb.append("create event ")
        .append(quote(subject)).append(" ")
        .append("from ").append(startStr).append(" ")
        .append("to ").append(endStr).append(" ");

    // Recurrence: "repeats MTWRF for N" or "repeats MTWRF until yyyy-MM-dd"
    if (weekdayPattern != null && !weekdayPattern.isEmpty()) {
      sb.append("repeats ")
          .append(weekdayPattern.toUpperCase())
          .append(" ");
    }

    if (occurrences != null) {
      sb.append("for ").append(occurrences).append(" ");
    } else if (endDate != null) {
      sb.append("until ").append(formatDate(endDate)).append(" ");
    }

    // Optional fields
    if (description != null && !description.isEmpty()) {
      sb.append("description ").append(quote(description)).append(" ");
    }

    if (location != null && !location.isEmpty()) {
      sb.append("location ").append(quote(location)).append(" ");
    }

    if (status != null) {
      sb.append("status ").append(status.name());
    }

    return sb.toString().trim();
  }

  // -------------------------------------------------------
  // Event editing (single)
  // -------------------------------------------------------

  @Override
  public String buildEditSingleEventCommand(String subject,
                                            ZonedDateTime start,
                                            ZonedDateTime end,
                                            String property,
                                            Object newValue) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);
    String valueStr = formatPropertyValue(property, newValue);

    // Matches EditEventCommand:
    // edit event <subject> from <start> to <end> with <property> <value>
    return "edit event "
        + quote(subject) + " "
        + "from " + startStr + " "
        + "to " + endStr + " "
        + "with " + property.toLowerCase() + " "
        + valueStr;
  }

  // -------------------------------------------------------
  // Event editing (series from this onward)
  // -------------------------------------------------------

  @Override
  public String buildEditSeriesFromThisOnwardCommand(String subject,
                                                     ZonedDateTime start,
                                                     ZonedDateTime end,
                                                     String property,
                                                     Object newValue) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);
    String valueStr = formatPropertyValue(property, newValue);

    // scope: "events" (EditEventCommand will interpret as FROM_THIS_ONWARD)
    return "edit events "
        + quote(subject) + " "
        + "from " + startStr + " "
        + "to " + endStr + " "
        + "with " + property.toLowerCase() + " "
        + valueStr;
  }

  // -------------------------------------------------------
  // Event editing (entire series)
  // -------------------------------------------------------

  @Override
  public String buildEditEntireSeriesCommand(String subject,
                                             ZonedDateTime start,
                                             ZonedDateTime end,
                                             String property,
                                             Object newValue) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);
    String valueStr = formatPropertyValue(property, newValue);

    // scope: "series" (EditEventCommand will interpret as ENTIRE_SERIES)
    return "edit series "
        + quote(subject) + " "
        + "from " + startStr + " "
        + "to " + endStr + " "
        + "with " + property.toLowerCase() + " "
        + valueStr;
  }


}

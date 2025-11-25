package calendar.controller;

import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Helper class containing shared string-building and formatting utilities.
 * for building CLI command strings.
 *
 * <p>This class exists to keep {@link CommandAdapter} small, clean,
 * and compliant with the Single Responsibility Principle.</p>
 *
 * <p>All responsibilities here are pure string/date formatting helpers,
 * never business logic.</p>
 */
public final class CommandStringHelper {

  private CommandStringHelper() {
    // prevent instantiation
  }

  /** Wraps string in quotes, escaping inner quotes. */
  public static String quote(String s) {
    if (s == null || s.isEmpty()) {
      return "\"\"";
    }
    return "\"" + s.replace("\"", "\\\"") + "\"";
  }

  /** Formats LocalDate as YYYY-MM-DD. */
  public static String formatDate(LocalDate date) {
    return date == null ? "" : date.toString();
  }

  /**
   * Format ZonedDateTime into local EST ISO-8601 WITHOUT timezone.
   * Example output: "2025-11-24T11:05"
   */
  public static String formatZdtLocalIso(ZonedDateTime zdt) {
    if (zdt == null) {
      return "";
    }
    LocalDateTime ldt = zdt.withZoneSameInstant(ParseUtils.EST).toLocalDateTime();
    return ldt.toString(); // ISO format
  }

  /**
   * Formats property update value safely.
   * Handles start/end time, status, text values.
   */
  public static String formatPropertyValue(String property, Object newValue) {
    if (newValue == null) {
      return "\"\"";
    }

    String prop = property.toLowerCase();

    if ("start".equals(prop) || "end".equals(prop)) {
      if (newValue instanceof ZonedDateTime) {
        String iso = formatZdtLocalIso((ZonedDateTime) newValue);
        return quote(iso);
      } else if (newValue instanceof LocalDateTime) {
        return quote(((LocalDateTime) newValue).toString());
      } else {
        return quote(newValue.toString());
      }
    }


    if ("status".equals(prop)) {
      return quote(newValue.toString().toUpperCase());
    }


    return quote(newValue.toString());
  }

  /**
   * Builds the common prefix for.
   * - edit event
   * - edit events (from-this-onward)
   * - edit series (entire series)
   */
  public static String buildEditPrefix(String scope,
                                       String subject,
                                       ZonedDateTime start,
                                       ZonedDateTime end) {

    String startStr = formatZdtLocalIso(start);
    String endStr = formatZdtLocalIso(end);

    return "edit "
        + scope + " "
        + quote(subject) + " "
        + "from " + startStr + " "
        + "to " + endStr + " ";
  }
}

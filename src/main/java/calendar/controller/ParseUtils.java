package calendar.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A utility class containing static helper methods for parsing and validating
 * command elements in the Virtual Calendar application.
 *
 * <p>This class cannot be instantiated.</p>
 */
public final class ParseUtils {

  private ParseUtils() {
    // prevent instantiation
  }

  /** Common date-time patterns accepted for parsing user input. */
  private static final DateTimeFormatter[] DT_PATTERNS = new DateTimeFormatter[]{
      DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  };

  /** Default timezone used by the calendar system (EST). */
  public static final ZoneId EST = ZoneId.of("America/New_York");

  /**
   * Parses a date string into a {@link LocalDate} object.
   *
   * @param s The string representing the date to be parsed (e.g., "YYYY-MM-DD").
   * @return The resulting {@link LocalDate} object.
   * @throws java.time.format.DateTimeParseException if the string cannot be parsed.
   */
  public static LocalDate parseDate(String s) {
    return LocalDate.parse(s.trim());
  }

  /**
   * Parses a date-time string into a {@link ZonedDateTime} using the EST timezone.
   *
   * <p>This method is flexible with user inputs: both "YYYY-MM-DD HH:mm" and
   * "YYYY-MM-DDTHH:mm" are supported. It iterates over multiple patterns
   * defined in {@code DT_PATTERNS}. If all fail, a final parse attempt is made
   * using {@link LocalDateTime#parse(CharSequence)} which may throw an exception.</p>
   *
   * @param s The date-time string to be parsed.
   * @return The resulting {@link ZonedDateTime} object in the EST zone.
   * @throws java.time.format.DateTimeParseException if no pattern matches the input.
   */
  public static ZonedDateTime parseDateTimeEst(String s) {
    String norm = s.trim().replace(' ', 'T');
    for (DateTimeFormatter f : DT_PATTERNS) {
      try {
        return LocalDateTime.parse(norm, f).atZone(EST);
      } catch (Exception ignore) {
        // Checkstyle Fix: The exception is intentionally ignored here because
        // the code is inside a loop that attempts multiple parsing patterns.
        // Failure simply means moving on to the next pattern.
      }
    }
    return LocalDateTime.parse(norm).atZone(EST);
  }

  /**
   * Parses the event subject from the given token list.
   * Supports quoted subjects such as {@code "Team Meeting"}.
   *
   * @param tokens The list of input tokens.
   * @param startIndex The starting index of the subject token.
   * @return The subject text without quotes.
   * @throws IllegalArgumentException if the quotes are not closed properly.
   */
  public static String parsePossiblyQuotedSubject(List<String> tokens, int startIndex) {
    String first = tokens.get(startIndex);
    if (first.startsWith("\"")) {
      StringBuilder sb = new StringBuilder();
      for (int i = startIndex; i < tokens.size(); i++) {
        if (i > startIndex) {
          sb.append(' ');
        }
        sb.append(tokens.get(i));
        if (tokens.get(i).endsWith("\"")) {
          String q = sb.toString();
          return q.substring(1, q.length() - 1);
        }
      }
      throw new IllegalArgumentException("Unclosed quoted subject.");
    } else {
      return first;
    }
  }

  /**
   * Parses weekday abbreviations (e.g., "MRU" or "MON,WED,FRI") into a set of {@link DayOfWeek}.
   *
   * @param s The weekday string (letters or comma-separated).
   * @return A {@link Set} of corresponding {@link DayOfWeek} values.
   * @throws IllegalArgumentException if an invalid weekday letter is encountered.
   */
  public static Set<DayOfWeek> parseWeekdays(String s) {
    String cleaned = s.trim().toUpperCase(Locale.ROOT).replace(",", "");
    Map<Character, DayOfWeek> map = Map.of(
        'M', DayOfWeek.MONDAY,
        'T', DayOfWeek.TUESDAY,
        'W', DayOfWeek.WEDNESDAY,
        'R', DayOfWeek.THURSDAY,
        'F', DayOfWeek.FRIDAY,
        'S', DayOfWeek.SATURDAY,
        'U', DayOfWeek.SUNDAY
    );
    Set<DayOfWeek> out = new LinkedHashSet<>();
    for (char c : cleaned.toCharArray()) {
      DayOfWeek d = map.get(c);
      if (d == null) {
        throw new IllegalArgumentException("Invalid weekday letter: " + c);
      }
      out.add(d);
    }
    return out;
  }

  /**
   * Splits a command line string into tokens while preserving quoted segments.
   * Example: {@code create event "Team Meeting" from 2025-05-01T10:00}
   * will produce {@code ["create", "event", "\"Team Meeting\"", "from", "2025-05-01T10:00"]}.
   *
   * @param line The raw command line string.
   * @return A list of tokens, with quoted phrases kept intact.
   */
  public static List<String> splitKeepingQuotes(String line) {
    List<String> out = new ArrayList<>();
    boolean inQuote = false;
    StringBuilder cur = new StringBuilder();
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch == '"') {
        inQuote = !inQuote;
        cur.append(ch);
      } else if (Character.isWhitespace(ch) && !inQuote) {
        if (cur.length() > 0) {
          out.add(cur.toString());
          cur.setLength(0);
        }
      } else {
        cur.append(ch);
      }
    }
    if (cur.length() > 0) {
      out.add(cur.toString());
    }
    return out;
  }
}

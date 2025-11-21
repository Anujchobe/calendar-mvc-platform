package calendar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing raw command-line input into tokens.
 *
 * <p>This tokenizer splits a line into whitespace-separated tokens,
 * while preserving quoted phrases as single tokens.</p>
 *
 * <p>Example:</p>
 * <pre>
 * Input : create event "Team Meeting" from 2025-11-10T09:00 to 2025-11-10T10:00
 * Output: [create, event, Team Meeting, from, 2025-11-10T09:00, to, 2025-11-10T10:00]
 * </pre>
 *
 * <p>This utility does not interpret token meaning (dates, enums, etc.);
 * that is handled by {@link ParseUtils}. It simply prepares the raw
 * syntax for {@link CommandFactory} to dispatch to the correct command.</p>
 */
public final class CommandUtils {

  private CommandUtils() {
  }

  /**
   * Splits a raw command string into tokens.
   *
   * <p>Quoted substrings (e.g., "Team Meeting") are treated as single tokens.
   * Quotes are stripped from the resulting tokens.</p>
   *
   * @param line the raw input line (e.g. user-entered command)
   * @return a list of cleaned tokens, never null
   */
  public static List<String> tokenize(String line) {
    List<String> tokens = new ArrayList<>();
    if (line == null || line.isBlank()) {
      return tokens;
    }

    Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
    Matcher matcher = pattern.matcher(line);

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else {
        tokens.add(matcher.group(2));
      }
    }

    return tokens;
  }

  /**
   * Joins tokens back into a single string for debugging or logging.
   *
   * @param tokens list of tokens
   * @return joined string
   */
  public static String joinTokens(List<String> tokens) {
    return String.join(" ", tokens);
  }
}

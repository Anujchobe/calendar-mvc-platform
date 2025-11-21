package calendar.controller;

import java.util.List;

/**
 * Dispatcher for "export" commands.
 *
 * <p>Usage examples:</p>
 * <ul>
 *   <li>{@code export calendar work_calendar.csv}</li>
 *   <li>{@code export calendar personal_calendar.ical}</li>
 * </ul>
 */
final class ExportDispatch {

  private ExportDispatch() {
    // Prevent instantiation
  }

  /**
   * Parses "export" commands and returns the correct {@link Command}.
   * Supported syntaxes:
   * export calendar filename.csv|filename.ical|filename.ics
   *
   * @param tokens tokenized user input
   * @return an {@link ExportCalCommand} ready for execution
   */
  static Command fromTokens(List<String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Empty export command.");
    }

    if (tokens.size() >= 3 && tokens.get(1).equalsIgnoreCase("calendar")) {
      return new ExportCalCommand(List.of(tokens.get(2)));
    }

    throw new IllegalArgumentException(
        "Usage: export calendar <filename.csv|filename.ical|filename.ics>");
  }
}

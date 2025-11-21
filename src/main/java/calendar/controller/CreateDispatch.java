package calendar.controller;

import java.util.List;

/**
 * Dispatcher for "create" commands.
 *
 * <p>Supported variants:</p>
 * <ul>
 *   <li>{@code create calendar --name <name> --timezone <tz>}</li>
 *   <li>{@code create event "Subject" from <start> to <end> ...}</li>
 * </ul>
 *
 * <p>This dispatcher ensures correct delegation to
 * {@link CreateCalendarCommand} or {@link CreateEventCommand}
 * depending on the second token.</p>
 */
final class CreateDispatch {

  // Prevent instantiation
  private CreateDispatch() {}

  /**
   * Parses the given token list and returns the appropriate
   * {@link Command} implementation.
   *
   * @param t tokenized user input
   * @return command instance
   * @throws IllegalArgumentException if syntax is invalid
   */
  static Command fromTokens(List<String> t) {
    if (t == null || t.size() < 2) {
      throw new IllegalArgumentException("Usage: create event|calendar ...");
    }

    String second = t.get(1).toLowerCase();

    switch (second) {
      case "event":

        return new CreateEventCommand(t);

      case "calendar":

        return new CreateCalendarCommand(t);

      default:
        throw new IllegalArgumentException("Usage: create event|calendar ...");
    }
  }
}

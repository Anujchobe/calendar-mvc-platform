package calendar.controller;

import java.util.List;

/**
 * Dispatcher for "print" commands.
 *
 * <p>Determines which print command variant to execute based on user input:
 * <ul>
 *   <li>{@code print events on <date>}</li>
 *   <li>{@code print events from <start> to <end>}</li>
 * </ul>
 */
final class PrintDispatch {

  // Prevent instantiation
  private PrintDispatch() {}

  /**
   * Parses the given tokens and returns the appropriate {@link Command}
   * implementation for the "print" command.
   *
   * @param tokens tokenized user input
   * @return the corresponding {@link Command}
   * @throws IllegalArgumentException if the command syntax is invalid
   */
  static Command fromTokens(List<String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Empty print command.");
    }

    if (tokens.size() >= 4 && tokens.get(1).equalsIgnoreCase("events")) {

      if (tokens.get(2).equalsIgnoreCase("on")) {
        return new PrintEventsOnCommand(List.of(tokens.get(3)));
      }

      if (tokens.size() >= 6
          && tokens.get(2).equalsIgnoreCase("from")
          && tokens.get(4).equalsIgnoreCase("to")) {
        return new PrintEventsRangeCommand(List.of(tokens.get(3), tokens.get(5)));
      }
    }

    throw new IllegalArgumentException(
        "Usage: print events on <date> | print events from <start> to <end>");
  }
}

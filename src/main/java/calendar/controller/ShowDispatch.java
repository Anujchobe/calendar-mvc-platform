package calendar.controller;

import java.util.List;

/**
 * Dispatcher for "show" commands.
 *
 * <p>Currently supports:</p>
 * <ul>
 *   <li>{@code show status on &lt;dateTime&gt;}</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>show status on 2025-11-09T09:30</pre>
 */
final class ShowDispatch {

  private ShowDispatch() {
    // Prevent instantiation — utility class
  }

  /**
   * Parses "show" command tokens and returns the correct {@link Command}.
   *
   * @param tokens list of user input tokens
   * @return a {@link ShowStatusCommand} instance
   */
  static Command fromTokens(List<String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Empty 'show' command.");
    }

    if (tokens.size() >= 4
        && tokens.get(1).equalsIgnoreCase("status")
        && tokens.get(2).equalsIgnoreCase("on")) {

      return new ShowStatusCommand(List.of("on", tokens.get(3)));
    }

    throw new IllegalArgumentException("Usage: show status on <dateTime>");
  }
}

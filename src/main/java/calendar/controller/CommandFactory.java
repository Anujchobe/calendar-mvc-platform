package calendar.controller;

import java.util.List;

/**
 * Factory that maps user command tokens to the appropriate {@link Command} implementation.
 *
 * <p>Supports both calendar-level commands (create/edit/use) and event-level commands
 * (print/export/show/copy).</p>
 */
public final class CommandFactory {

  private CommandFactory() {
  }

  /**
   * Parses a list of command tokens into a specific {@code Command} object.
   *
   * @param tokens The list of string tokens representing the command and its arguments.
   * @return A concrete {@code Command} implementation corresponding to the first token.
   * @throws IllegalArgumentException if the list of tokens is {@code null} or empty.
   */
  public static Command parseCommand(List<String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Empty command input.");
    }

    String first = tokens.get(0).toLowerCase();

    switch (first) {



      case "create":
        if (tokens.contains("calendar")) {
          return new CreateCalendarCommand(tokens);
        } else {
          return new CreateEventCommand(tokens);
        }

      case "edit":
        if (tokens.contains("calendar")) {
          return new EditCalendarCommand(tokens);
        } else {
          String scope = "event"; // default
          if (tokens.size() > 1) {
            String maybeScope = tokens.get(1).toLowerCase();
            if (maybeScope.equals("event") || maybeScope.equals("events")
                || maybeScope.equals("series")) {
              scope = maybeScope;
            }
          }
          return new EditEventCommand(tokens, scope);
        }

      case "use":
        if (tokens.contains("calendar")) {
          return new UseCalendarCommand(tokens);
        }
        throw new IllegalArgumentException(
            "Invalid syntax. " + "Usage: use calendar --name <calendarName>");

      case "copy":
        return new CopyCommand(tokens);

      case "print":
        return PrintDispatch.fromTokens(tokens);

      case "show":
        return ShowDispatch.fromTokens(tokens);

      case "export":
        return ExportDispatch.fromTokens(tokens);

      case "exit":
        return new ExitCommand();

      default:
        throw new IllegalArgumentException("Unknown command: " + first);
    }
  }
}

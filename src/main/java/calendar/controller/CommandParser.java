package calendar.controller;

import calendar.model.IcalendarManager;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * The main controller responsible for parsing and executing user commands.
 *
 * <p>This parser reads text-based commands from any {@link Readable} source,
 * converts them into {@link Command} objects using {@link CommandFactory},
 * and executes them against the provided {@link IcalendarManager} model.</p>
 */
public class CommandParser {

  private final IcalendarManager manager;
  private final Readable input;
  private final Appendable output;

  /**
   * Constructs a {@code CommandParser}.
   *
   * @param manager The calendar manager (model layer) that maintains all calendars.
   * @param input   The input source from which commands are read (e.g., System.in or a file).
   * @param output  The output destination for messages and errors.
   */
  public CommandParser(IcalendarManager manager, Readable input, Appendable output) {
    if (manager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null.");
    }
    if (input == null || output == null) {
      throw new IllegalArgumentException("Input and output streams cannot be null.");
    }

    this.manager = manager;
    this.input = input;
    this.output = output;
  }

  /**
   * Runs the command parsing loop: reads commands, parses them, and executes them.
   *
   * <p>This loop is used in both interactive and headless modes.
   * Errors are printed but do not terminate execution unless they are I/O related.</p>
   */
  public void run() {
    Scanner scanner = new Scanner(input);

    try {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }

        List<String> tokens = CommandUtils.tokenize(line);

        try {
          Command command = CommandFactory.parseCommand(tokens);
          command.execute(manager);

        } catch (ExitCommand.ExitSignal exit) {
          // clean, intentional exit — DO NOT PRINT ERROR
          return;

        } catch (Exception e) {
          output.append("Error: ")
              .append(e.getMessage())
              .append(System.lineSeparator());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("I/O error during command execution", e);
    } finally {
      scanner.close();
    }
  }

}

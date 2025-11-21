package calendar;

import calendar.controller.CommandParser;
import calendar.model.CalendarManagerImpl;
import calendar.model.IcalendarManager;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * **Entry point** for the Virtual Calendar application.
 *
 * <p>This class initializes the application environment and controls the
 * main execution loop based on command-line arguments (interactive or headless mode).</p>
 */
public class CalendarRunner {

  /**
   * Main entry point for the calendar application.
   *
   * <p>This method initializes the system, handles command-line arguments to
   * determine the operational mode (interactive or headless), and starts
   * the command processing loop.</p>
   *
   * @param args Command line arguments, typically used to
   *             specify the application's mode and any input file.
   */
  public static void main(String[] args) {

    // ----------- 1. Missing or invalid mode ----------
    if (args.length < 2 || !"--mode".equalsIgnoreCase(args[0])) {
      System.out.println("Usage: java -jar calendar.jar --mode [interactive|headless <file>]");
      return;
    }

    final String mode = args[1].toLowerCase(Locale.ROOT);

    // ----------- 2. Unknown mode ----------
    if (!mode.equals("interactive") && !mode.equals("headless")) {
      System.out.println("Unknown mode: " + mode);
      return;
    }

    final IcalendarManager manager = new CalendarManagerImpl();
    Readable input;
    Appendable output = System.out;

    try {

      // ----------- 3. Interactive mode ----------
      if (mode.equals("interactive")) {
        input = new InputStreamReader(System.in);
      } else { // mode == "headless"
        if (args.length < 3) {
          System.out.println("Missing command file for headless mode.");
          return;
        }

        try {
          input = new FileReader(args[2]);
        } catch (FileNotFoundException e) {
          System.out.println("Command file not found: " + args[2]);
          return;
        }
      }

      // ----------- 5. Run parser ----------
      CommandParser parser = new CommandParser(manager, input, output);
      parser.run();

    } catch (Exception e) {
      System.out.println("Fatal error: " + e.getMessage());
    }
  }
}
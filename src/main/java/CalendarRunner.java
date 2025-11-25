import calendar.controller.CommandAdapter;
import calendar.controller.CommandParser;
import calendar.controller.GuiFeaturesController;
import calendar.controller.IcommandAdapter;
import calendar.model.CalendarManagerImpl;
import calendar.model.IcalendarManager;
import calendar.view.Icalendarview;
import calendar.view.SwingCalendarView;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Main application runner for the Virtual Calendar system.
 *
 * <p><b>Architecture:</b></p>
 * <ul>
 *     <li><b>GUI Mode:</b> View → GuiFeaturesController → IcommandAdapter →
 *         CommandUtils/CommandFactory → Commands → Model</li>
 *     <li><b>CLI Mode:</b> CommandParser → CommandUtils/CommandFactory →
 *         Commands → Model</li>
 * </ul>
 *
 * <p>Both modes use the SAME command parsing and execution infrastructure,
 * ensuring identical behavior.</p>
 */
public class CalendarRunner {

  /**
   * Main entry point for the application.
   *
   * @param args Command line arguments specifying mode and optional script file
   */
  public static void main(String[] args) {


    if (args.length == 0) {
      launchGui();
      return;
    }


    if (!"--mode".equalsIgnoreCase(args[0])) {
      printUsage("Missing or invalid --mode flag.");
      return;
    }

    if (args.length < 2) {
      printUsage("Missing mode value after --mode.");
      return;
    }

    final String mode = args[1].toLowerCase(Locale.ROOT);

    switch (mode) {
      case "interactive":
        runInteractive();
        break;

      case "headless":
        if (args.length < 3) {
          printUsage("Missing script file path for headless mode.");
          return;
        }
        runHeadless(args[2]);
        break;

      default:
        printUsage("Unknown mode: " + mode);
    }
  }

  /**
   * Launches GUI mode with command adapter architecture.
   *
   * <p><b>Flow:</b></p>
   * <pre>
   * SwingCalendarView (View)
   *   ↓ fires events (e.g., createSingleEvent(...))
   * GuiFeaturesController (implements Ifeatures)
   *   ↓ calls IcommandAdapter.buildXxxCommand(...)
   * CommandAdapter
   *   ↓ returns command string (e.g., "create event 'Meeting' from ...")
   * GuiFeaturesController.executeCommand(commandString)
   *   ↓ CommandUtils.tokenize()
   *   ↓ CommandFactory.parseCommand()
   * CreateEventCommand (existing CLI command)
   *   ↓ command.execute(manager)
   * CalendarManagerImpl (Model)
   * </pre>
   */
  private static void launchGui() {


    if (java.awt.GraphicsEnvironment.isHeadless()) {
      System.out.println("GUI mode not available in headless environment.");
      return;
    }

    IcalendarManager manager = new CalendarManagerImpl();
    IcommandAdapter adapter = new CommandAdapter();


    Icalendarview view = new SwingCalendarView();

    new GuiFeaturesController(manager, adapter, view);

    view.display();
  }




  /**
   * Runs interactive CLI mode.
   *
   * <p>CommandParser reads user input, tokenizes it, parses via CommandFactory,
   * and executes commands against the model.</p>
   */
  private static void runInteractive() {
    IcalendarManager manager = new CalendarManagerImpl();
    Readable input = new InputStreamReader(System.in);
    Appendable output = System.out;

    CommandParser parser = new CommandParser(manager, input, output);
    parser.run();
  }

  /**
   * Runs headless CLI mode from a script file.
   *
   * @param scriptPath path to script file containing commands
   */
  private static void runHeadless(String scriptPath) {
    IcalendarManager manager = new CalendarManagerImpl();
    Appendable output = System.out;
    Readable input;

    try {
      input = new FileReader(scriptPath);
    } catch (FileNotFoundException e) {
      System.out.println("Command file not found: " + scriptPath);
      return;
    }

    CommandParser parser = new CommandParser(manager, input, output);
    parser.run();
  }

  /**
   * Prints usage information.
   *
   * @param message error message
   */
  private static void printUsage(String message) {
    System.out.println(message);
    System.out.println("Usage:");
    System.out.println("  java -jar calendar.jar                 # GUI mode");
    System.out.println("  java -jar calendar.jar --mode interactive");
    System.out.println("  java -jar calendar.jar --mode headless path-of-script-file");
  }
}
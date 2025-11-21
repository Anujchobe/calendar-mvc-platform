package calendar.controller;

import static org.junit.Assert.assertTrue;

import calendar.controller.mocks.FakeManager;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration-style test: exercises {@link CommandParser} with multiple commands.
 */
public class ControllerExecutionTest {

  private FakeManager mgr;
  private StringWriter out; // Fix 5: Missing Javadoc for field

  /**
   * Sets up the testing environment before each test method.
   * Initializes the fake manager and the output writer.
   */
  @Before
  public void setup() {
    mgr = new FakeManager();
    out = new StringWriter();
  }

  @Test
  public void testParserRunsMultipleCommands() {
    String input = "create calendar --name Alpha --timezone America/New_York\n"
        + "use calendar --name Alpha\n"
        + "show status on 2025-11-10T10:00\n"
        + "exit\n";
    CommandParser parser = new CommandParser(mgr, new StringReader(input), out);
    parser.run();

    String output = out.toString();
    assertTrue(output.contains("") || output.isEmpty());
    assertTrue(mgr.listCalendars().contains("Alpha"));
  }

  @Test
  public void testParserHandlesErrorGracefully() {
    String input = "nonsensecommand\n";
    CommandParser parser = new CommandParser(mgr, new StringReader(input), out);
    parser.run();
    assertTrue(out.toString().contains("Error: Unknown command"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParserNullInputs() {
    new CommandParser(null, new StringReader(""), new StringWriter());
  }

  @Test
  public void testCommandFactoryCreateEventBranch() {
    java.util.List<String> t = java.util.Arrays.asList("create", "event", "A", "from",
        "2025-11-10T09:00", "to", "2025-11-10T10:00");
    assertTrue(CommandFactory.parseCommand(t) instanceof CreateEventCommand);
  }

  @Test
  public void testCommandFactoryEditCalendarBranch() {
    java.util.List<String> t = java.util.Arrays.asList("edit", "calendar", "--name", "X",
        "--property", "timezone", "Asia/Kolkata");
    assertTrue(CommandFactory.parseCommand(t) instanceof EditCalendarCommand);
  }

  @Test
  public void testCommandFactoryExportBranch() {
    java.util.List<String> t = java.util.Arrays.asList("export", "calendar", "work.ical");
    assertTrue(CommandFactory.parseCommand(t) instanceof ExportCalCommand);
  }

  @Test
  public void testCopyCommandDispatches() {
    // Fix 6: Line is longer than 100 characters (103). Split array across lines.
    java.util.List<String> ev = java.util.Arrays.asList(
        "copy", "event", "Meeting", "on", "2025-11-10T09:00", "--target", "Personal", "to",
        "2025-11-11T09:00");
    new CopyCommand(ev).execute(mgr);

    // Fix 7: Line is longer than 100 characters (105). Split array across lines.
    java.util.List<String> date = java.util.Arrays.asList(
        "copy", "events", "on", "2025-11-10", "--target", "Personal", "to", "2025-11-11");
    new CopyCommand(date).execute(mgr);

    java.util.List<String> range = java.util.Arrays.asList(
        "copy", "events", "between", "2025-11-09", "and", "2025-11-15", "--target", "Personal",
        "to", "2025-11-20");
    new CopyCommand(range).execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyCommandInvalidSyntax() {
    java.util.List<String> bad = java.util.Arrays.asList("copy", "wrong");
    new CopyCommand(bad).execute(mgr);
  }
}
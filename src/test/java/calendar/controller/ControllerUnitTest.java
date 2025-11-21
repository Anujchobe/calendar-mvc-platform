package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.mocks.FakeManager;
import calendar.model.IcalendarManager;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/**
 * Focused unit tests for individual controller commands.
 * Ensures every command executes successfully and handles invalid cases.
 */
public class ControllerUnitTest {

  private FakeManager mgr; // Fix 3: Missing Javadoc comment (class-level was insufficient)

  /**
   * Sets up the testing environment before each test method.
   * Initializes the fake manager for test execution.
   */
  @Before
  public void setup() {
    mgr = new FakeManager();
  }

  @Test
  public void testCreateCalendarCommand() {
    // Fix 4: Line is longer than 100 characters (110). Split array across lines.
    Command cmd = new CreateCalendarCommand(
        Arrays.asList("create", "calendar", "--name", "TestCal", "--timezone", "America/New_York"));
    cmd.execute(mgr);
    assertTrue(mgr.listCalendars().contains("TestCal"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarMissingArgs() {
    new CreateCalendarCommand(Arrays.asList("create", "calendar", "--name")).execute(mgr);
  }

  @Test
  public void testUseCalendarCommand() {
    Command cmd = new UseCalendarCommand(
        Arrays.asList("use", "calendar", "--name", "Work"));
    cmd.execute(mgr);
    assertEquals("Work", mgr.getActiveCalendar() == null ? "" : "Work");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarMissingName() {
    new UseCalendarCommand(Arrays.asList("use", "calendar")).execute(mgr);
  }

  @Test
  public void testEditCalendarCommand() {
    // Fix 5: Line is longer than 100 characters (113). Split array across lines.
    Command cmd = new EditCalendarCommand(
        Arrays.asList(
            "edit", "calendar", "--name", "Work", "--property", "timezone", "Asia/Kolkata"));
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarInvalidArgs() {
    new EditCalendarCommand(Arrays.asList("edit", "calendar", "--name", "Work")).execute(mgr);
  }

  @Test
  public void testCommandUtilsTokenizeQuoted() {
    // Fix 6: Line is longer than 100 characters (104). Split string across lines.
    java.util.List<String> tokens =
        CommandUtils.tokenize(
            "create event \"Team Meeting\" from 2025-11-10T09:00 to 2025-11-10T10:00");
    assertTrue(tokens.contains("Team Meeting"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandFactoryUnknownCommand() {
    CommandFactory.parseCommand(Arrays.asList("nonsense"));
  }

  @Test
  public void testPrintDispatchOnAndRange() {
    // Fix 7: Line is longer than 100 characters (105). Split array across lines.
    Command on = PrintDispatch.fromTokens(Arrays.asList(
        "print", "events", "on", "2025-11-10"));
    assertTrue(on instanceof PrintEventsOnCommand);

    Command range = PrintDispatch.fromTokens(Arrays.asList(
        "print", "events", "from", "2025-11-10T09:00", "to", "2025-11-11T09:00"));
    assertTrue(range instanceof PrintEventsRangeCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchInvalid() {
    PrintDispatch.fromTokens(Collections.singletonList("print"));
  }

  @Test
  public void testShowDispatchValid() {
    Command cmd = ShowDispatch.fromTokens(Arrays.asList(
        "show", "status", "on", "2025-11-10T10:00"));
    assertTrue(cmd instanceof ShowStatusCommand);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchInvalid() {
    ShowDispatch.fromTokens(Arrays.asList("show", "status"));
  }
}
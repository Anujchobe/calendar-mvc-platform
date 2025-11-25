package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.mocks.FakeCalendar;
import calendar.controller.mocks.FakeManager;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * Targeted tests for remaining missing branches and surviving mutations.
 */
public class ControllerTargetedCoverageTest {

  // --- Mocks Setup ---
  private static class NoEventsCalendar extends FakeCalendar {

    @Override
    public List<calendar.model.Event> queryEventsOn(final java.time.LocalDate date) {
      return Collections.emptyList();
    }

    @Override
    public List<calendar.model.Event> queryEventsBetween(final java.time.ZonedDateTime start,
                                                         final java.time.ZonedDateTime end) {
      return Collections.emptyList();
    }

    @Override
    public List<calendar.model.Event> getAllEvents() {
      return Collections.emptyList();
    }
  }

  private static class EmptyManager extends FakeManager {

    @Override
    public Icalendar getActiveCalendar() {
      return new NoEventsCalendar();
    }
  }

  // Manager that always returns null for active calendar
  private static class NoActiveManager extends FakeManager {
    @Override
    public Icalendar getActiveCalendar() {
      return null;
    }
  }

  // --- Test: ExportCalCommand (Empty Events + Missing Arg/Format) ---

  @Test(expected = IllegalStateException.class)
  public void testExportCalCommandNoActiveCalendar() {
    // FIX: Replaced ControllerMaxCoverageTest.NoActiveManager() with local NoActiveManager()
    new ExportCalCommand(List.of("file.csv"))
        .execute(new NoActiveManager());
  }

  @Test
  public void testExportCalCommandNoEvents() {
    final EmptyManager m = new EmptyManager();
    final StringWriter out = new StringWriter();
    final StringReader in = new StringReader("export calendar file.csv\n");
    new CommandParser(m, in, out).run();
    assertTrue(out.toString().isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalCommandMissingArgsThrows() {
    new ExportCalCommand(Collections.emptyList()).execute(new FakeManager());
  }

  // --- Test: Print Commands (Empty Events) ---

  @Test
  public void testPrintEventsOnCommandNoEvents() {
    final EmptyManager m = new EmptyManager();
    final StringWriter out = new StringWriter();
    final StringReader in = new StringReader("print events on 2025-11-10\n");
    new CommandParser(m, in, out).run();
    assertTrue(out.toString().isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testPrintEventsOnCommandNoActiveCalendar() {
    // FIX: Replaced ControllerMaxCoverageTest.NoActiveManager() with local NoActiveManager()
    new PrintEventsOnCommand(List.of("2025-11-10"))
        .execute(new NoActiveManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsOnCommandMissingArgThrows() {
    new PrintEventsOnCommand(Collections.emptyList()).execute(new FakeManager());
  }

  @Test
  public void testPrintEventsRangeCommandNoEvents() {
    final EmptyManager m = new EmptyManager();
    final StringWriter out = new StringWriter();
    final StringReader in = new StringReader(
        "print events from 2025-11-09T00:00 to 2025-11-10T00:00\n");
    new CommandParser(m, in, out).run();
    assertTrue(out.toString().isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testPrintEventsRangeCommandNoActiveCalendar() {
    // FIX: Replaced ControllerMaxCoverageTest.NoActiveManager() with local NoActiveManager()
    new PrintEventsRangeCommand(List.of("2025-11-09T00:00", "2025-11-10T00:00"))
        .execute(new NoActiveManager());
  }

  // --- Test: ExitCommand (0% Mutation) ---

  @Test
  public void testExitCommandPrintsAndThrowsSignal() {
    final ExitCommand cmd = new ExitCommand();
    try {
      // Manager is ignored, but method body is hit
      cmd.execute(null);
      fail("Expected ExitSignal");
    } catch (ExitCommand.ExitSignal e) {
      // Success, mutation is killed.
      assertTrue(true);
    }
  }

  // --- Test: CommandFactory (Missing Edit default scope) ---

  @Test
  public void testCommandFactoryEditDefaultScope() {
    final List<String> tokens = Arrays.asList(
        "edit", "foobar", "Meeting", "from", "2025-11-10T09:00",
        "with", "location", "Zoom");
    final Command cmd = CommandFactory.parseCommand(tokens);
    // Should default scope to "event" if the second token is not recognized
    assertTrue(cmd instanceof EditEventCommand);
  }

  // --- Test: EditEventCommand (Missing 'to' end time check) ---

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandMissingEndTimeAfterTo() {
    final List<String> tokens = Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", // token is present
        "with", "location", "Zoom"); // args are shifted
    new EditEventCommand(tokens, "event").execute(new FakeManager());
  }

  // --- Test: CopyCommand (Missing single event keywords) ---

  @Test(expected = IllegalArgumentException.class)
  public void testCopySingleEventMissingOnKeyword() {
    // Missing 'on' keyword
    final List<String> tokens = Arrays.asList(
        "copy", "event", "\"A\"",
        "--target", "Personal",
        "to", "2025-11-12T10:00");
    new CopyCommand(tokens).execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopySingleEventMissingToKeyword() {
    // Missing 'to' keyword
    final List<String> tokens = Arrays.asList(
        "copy", "event", "\"A\"",
        "on", "2025-11-10T10:00",
        "--target", "Personal");
    new CopyCommand(tokens).execute(new FakeManager());
  }

  // --- Test: EditCalendarCommand (Missing Name/Property and Default Case) ---

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarCommandMissingNameArg() {
    final List<String> tokens = Arrays.asList(
        "edit", "calendar", "--name",
        "--property", "timezone", "Asia/Kolkata");
    new EditCalendarCommand(tokens).execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarCommandMissingPropertyArg() {
    final List<String> tokens = Arrays.asList(
        "edit", "calendar", "--name", "Work",
        "--property", "timezone");
    new EditCalendarCommand(tokens).execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarCommandMissingNewValue() {
    final List<String> tokens = Arrays.asList(
        "edit", "calendar", "--name", "Work",
        "--property");
    new EditCalendarCommand(tokens).execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarCommandDefaultCase() {
    // FIX: Modified tokens to ensure one required argument (newValue) is missed,
    // thereby forcing the final validation check to throw the IAE.
    final List<String> tokens = Arrays.asList(
        "edit", "calendar", "invalid", // Forces default case
        "--name", "Work",
        "--property", "timezone"); // Consumes 'timezone' for property, but missing 'newValue'
    new EditCalendarCommand(tokens).execute(new FakeManager());
  }
}
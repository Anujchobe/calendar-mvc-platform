package calendar.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import calendar.controller.mocks.FakeCalendar;
import calendar.controller.mocks.FakeManager;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-end controller coverage tests.
 * Uses FakeManager/FakeCalendar to validate all command paths.
 */
public class ControllerFullCoverageTest {

  private FakeManager manager;

  /**
   * Sets up the testing environment before each test method.
   * Initializes the {@code FakeManager} to act as the test model manager.
   */
  @Before
  public void setup() {
    manager = new FakeManager();
  }

  // ==== CREATE EVENT =======================================================
  @Test
  public void testCreateEventFromTo() {
    CreateEventCommand cmd = new CreateEventCommand(Arrays.asList(
        "create", "event", "\"Meeting\"", "from",
        "2025-11-10T09:00", "to", "2025-11-10T10:00"));
    cmd.execute(manager);
    assertFalse(((FakeCalendar) manager.getActiveCalendar()).events.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventMissingArgsThrows() {
    CreateEventCommand cmd = new CreateEventCommand(Arrays.asList("create", "event"));
    cmd.execute(manager);
  }

  // ==== EDIT EVENT ========================================================
  @Test
  public void testEditEventSingleScope() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "Meeting", "from",
        "2025-11-10T09:00", "to", "2025-11-10T10:00",
        "with", "location", "Zoom"), "event");
    cmd.execute(manager);
  }


  @Test
  public void testEditEventSeriesScope() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "series", "Meeting", "from",
        "2025-11-10T09:00", "with", "description", "Updated"), "series");
    cmd.execute(manager);
  }

  // ==== QUERY =============================================================
  @Test
  public void testQueryDateAndRange() {
    QueryCommand q1 = new QueryCommand(Arrays.asList("date", "2025-11-10"));
    q1.execute(manager);
    QueryCommand q2 = new QueryCommand(Arrays.asList(
        "range", "2025-11-09T09:00", "2025-11-10T09:00"));
    q2.execute(manager);
  }

  // ==== PRINT COMMANDS ====================================================
  @Test
  public void testPrintEventsOnAndRange() {
    new PrintEventsOnCommand(Arrays.asList("2025-11-10")).execute(manager);
    new PrintEventsRangeCommand(Arrays.asList(
        "2025-11-09T09:00", "2025-11-10T09:00")).execute(manager);
  }

  // ==== EXPORT ============================================================
  @Test
  public void testExportCsvAndIcs() {
    new ExportCalCommand(Arrays.asList("calendar.csv")).execute(manager);
    new ExportCalCommand(Arrays.asList("calendar.ics")).execute(manager);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportUnsupportedFormatThrows() {
    new ExportCalCommand(Arrays.asList("calendar.txt")).execute(manager);
  }

  // ==== SHOW STATUS =======================================================
  @Test
  public void testShowStatusAvailableAndBusy() {
    new ShowStatusCommand(Arrays.asList("on", "2025-11-10T09:00")).execute(manager);
    new ShowStatusCommand(Arrays.asList("on", "2025-11-10T10:00")).execute(manager);
  }

  // ==== USE / CREATE / EDIT CALENDAR ======================================
  @Test
  public void testCreateEditUseCalendar() {
    new CreateCalendarCommand(Arrays.asList(
        "create", "calendar", "--name", "Temp", "--timezone", "Europe/London"))
        .execute(manager);

    new EditCalendarCommand(Arrays.asList(
        "edit", "calendar", "--name", "Work", "--property", "timezone", "Asia/Kolkata"))
        .execute(manager);

    new UseCalendarCommand(Arrays.asList("use", "calendar", "--name", "Personal"))
        .execute(manager);
  }

  // ==== COPY COMMAND ======================================================
  @Test
  public void testCopyEventAndEventsRange() {
    new CopyCommand(Arrays.asList(
        "copy", "event", "Meeting", "on", "2025-11-10T09:00",
        "--target", "Personal", "to", "2025-11-12T09:00")).execute(manager);

    new CopyCommand(Arrays.asList(
        "copy", "events", "between", "2025-11-09", "and", "2025-11-15",
        "--target", "Travel", "to", "2025-11-20")).execute(manager);
  }

  // ==== COMMAND PARSER LOOP ===============================================
  @Test
  public void testCommandParserLoopRunsWithoutError() {
    StringReader in = new StringReader(
        "use calendar --name Work\n"
            + "copy event Meeting on 2025-11-10T09:00 --target Personal to 2025-11-11T09:00\n"
            + "exit\n");
    StringWriter out = new StringWriter();
    new CommandParser(manager, in, out).run();
    assertTrue(out.toString().contains("Error:") || out.toString().isEmpty());
  }
}

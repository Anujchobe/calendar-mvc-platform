package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.model.EditMode;
import calendar.model.Event;
import calendar.model.EventKey;
import calendar.model.EventStatus;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import calendar.model.RecurrenceRule;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite to achieve 100% branch coverage and 100% mutation coverage
 * for the calendar.controller package.
 */
public class CompleteMutationKillerTest {

  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;
  private PrintStream originalOut;
  private PrintStream originalErr;

  /**
   * Stub calendar implementation.
   */
  private static class StubCalendar implements Icalendar {
    final List<Event> events = new ArrayList<>();
    ZoneId zone = ZoneId.of("America/New_York");
    final List<String> calls = new ArrayList<>();

    @Override
    public void createEvent(Event e) {
      events.add(e);
    }

    @Override
    public void createSeries(Event e, RecurrenceRule rule) {
      events.add(e);
    }

    @Override
    public void editEvent(EventKey key, String property, Object newValue) {
      calls.add("editEvent");
    }

    @Override
    public void editSeries(EventKey key, String property, Object newValue,
                           EditMode mode) {
      calls.add("editSeries");
    }

    @Override
    public List<Event> queryEventsOn(LocalDate date) {
      return events;
    }

    @Override
    public List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      return events;
    }

    @Override
    public boolean isBusy(ZonedDateTime timestamp) {
      calls.add("busy");
      return timestamp.getHour() % 2 == 0;
    }

    @Override
    public List<Event> getAllEvents() {
      return new ArrayList<>(events);
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public void setZone(ZoneId zone) {
      this.zone = zone;
    }
  }

  /**
   * Stub manager implementation.
   */
  private static class StubManager implements IcalendarManager {
    final Map<String, Icalendar> calendars = new HashMap<>();
    Icalendar active;
    String activeName;

    StubManager() {
      calendars.put("Work", new StubCalendar());
      calendars.put("Personal", new StubCalendar());
      active = calendars.get("Work");
      activeName = "Work";
    }

    @Override
    public void createCalendar(String name, ZoneId zone) {
      calendars.put(name, new StubCalendar());
    }

    @Override
    public void editCalendar(String name, String property, String newValue) {
      // no-op
    }

    @Override
    public void useCalendar(String name) {
      Icalendar cal = calendars.get(name);
      if (cal == null) {
        throw new IllegalArgumentException("Calendar not found: " + name);
      }
      active = cal;
      activeName = name;
    }

    @Override
    public Icalendar getActiveCalendar() {
      return active;
    }

    @Override
    public Icalendar getCalendar(String name) {
      return calendars.get(name);
    }

    @Override
    public List<String> listCalendars() {
      return new ArrayList<>(calendars.keySet());
    }
  }

  /**
   * Fake view for GUI controller tests.
   */
  private static class FakeView implements calendar.view.Icalendarview {
    int refreshCalls = 0;

    @Override
    public void display() {
      // no-op
    }

    @Override
    public void refresh() {
      refreshCalls++;
    }

    @Override
    public void showError(String message) {
      // no-op
    }

    @Override
    public void showSuccess(String message) {
      // no-op
    }

    @Override
    public void setFeatures(calendar.controller.Ifeatures features) {
      // no-op
    }
  }

  /**
   * Sets up output capture.
   */
  @Before
  public void setup() {
    outContent = new ByteArrayOutputStream();
    errContent = new ByteArrayOutputStream();
    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Restores original output.
   */
  @After
  public void teardown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  // ========================================================================
  // COMMANDFACTORY: Missing branches (lines 24, 46)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCommandFactoryNullTokens() {
    CommandFactory.parseCommand(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandFactoryEmptyTokens() {
    CommandFactory.parseCommand(Collections.emptyList());
  }

  @Test
  public void testCommandFactoryEditWithExactlyOneToken() {
    List<String> tokens = Collections.singletonList("edit");
    Command cmd = CommandFactory.parseCommand(tokens);
    assertTrue(cmd instanceof EditEventCommand);

    try {
      cmd.execute(new StubManager());
      fail("Expected exception on execute");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid"));
    }
  }

  @Test
  public void testCommandFactoryEditWithTwoTokensUnknownScope() {
    List<String> tokens = List.of("edit", "unknown");
    Command cmd = CommandFactory.parseCommand(tokens);
    assertTrue(cmd instanceof EditEventCommand);
  }

  // ========================================================================
  // COMMANDPARSER: Missing branches (lines 32, 53, 73)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNullInput() {
    new CommandParser(new StubManager(), null, new java.io.StringWriter());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNullOutput() {
    new CommandParser(new StubManager(), new java.io.StringReader(""), null);
  }

  @Test
  public void testCommandParserHandlesEmptyLines() {
    StubManager mgr = new StubManager();
    java.io.StringReader input = new java.io.StringReader("\n  \n\t\nexit\n");
    java.io.StringWriter output = new java.io.StringWriter();

    CommandParser parser = new CommandParser(mgr, input, output);
    parser.run();

    assertTrue(output.toString().isEmpty());
  }

  @Test
  public void testCommandParserHandlesIoException() {
    StubManager mgr = new StubManager();

    // Create an Appendable that throws IOException
    java.io.StringReader input = new java.io.StringReader("invalid\n");

    Appendable badOutput = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws java.io.IOException {
        throw new java.io.IOException("Forced IO error");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end)
          throws java.io.IOException {
        throw new java.io.IOException("Forced IO error");
      }

      @Override
      public Appendable append(char c) throws java.io.IOException {
        throw new java.io.IOException("Forced IO error");
      }
    };

    CommandParser parser = new CommandParser(mgr, input, badOutput);

    try {
      parser.run();
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("I/O error"));
    }
  }

  // ========================================================================
  // COMMANDADAPTER: Missing branches (lines 47, 52, 86, 94, 99, 104)
  // ========================================================================

  @Test
  public void testCommandAdapterSingleEventWithNullDescription() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateSingleEventCommand(
        "Meeting", start, start.plusHours(1), null, "Zoom", EventStatus.PUBLIC
    );

    assertFalse(cmd.contains("description"));
    assertTrue(cmd.contains("location"));
  }

  @Test
  public void testCommandAdapterSingleEventWithEmptyLocation() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateSingleEventCommand(
        "Meeting", start, start.plusHours(1), "Desc", "", EventStatus.PUBLIC
    );

    assertTrue(cmd.contains("description"));
    assertFalse(cmd.contains("location"));
  }

  @Test
  public void testCommandAdapterRecurringEventWithNullWeekdayPattern() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Meeting", start, start.plusHours(1), "Desc", "Loc",
        EventStatus.PUBLIC, null, 5, null
    );

    assertFalse(cmd.contains("repeats"));
    assertTrue(cmd.contains("for 5"));
  }

  @Test
  public void testCommandAdapterRecurringEventWithEmptyWeekdayPattern() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Meeting", start, start.plusHours(1), "Desc", "Loc",
        EventStatus.PUBLIC, "", 5, null
    );

    assertFalse(cmd.contains("repeats"));
  }

  @Test
  public void testCommandAdapterRecurringEventWithNullOccurrencesUsesEndDate() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Meeting", start, start.plusHours(1), "Desc", "Loc",
        EventStatus.PUBLIC, "MTWRF", null, LocalDate.of(2025, 12, 31)
    );

    assertTrue(cmd.contains("until"));
    assertFalse(cmd.contains("for "));
  }

  @Test
  public void testCommandAdapterRecurringEventWithNullDescriptionAndLocation() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Meeting", start, start.plusHours(1), null, null,
        EventStatus.PUBLIC, "MTWRF", 5, null
    );

    assertFalse(cmd.contains("description"));
    assertFalse(cmd.contains("location"));
  }

  @Test
  public void testCommandAdapterRecurringEventWithEmptyDescriptionAndLocation() {
    CommandAdapter adapter = new CommandAdapter();
    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));

    String cmd = adapter.buildCreateRecurringEventCommand(
        "Meeting", start, start.plusHours(1), "", "",
        EventStatus.PUBLIC, "MTWRF", 5, null
    );

    assertFalse(cmd.contains("description"));
    assertFalse(cmd.contains("location"));
  }

  // ========================================================================
  // CREATECALENDARCOMMAND: Missing branches (line 42)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandMissingName() {
    new CreateCalendarCommand(List.of("create", "calendar", "--timezone",
        "America/New_York")).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandMissingTimezone() {
    new CreateCalendarCommand(List.of("create", "calendar", "--name",
        "Test")).execute(new StubManager());
  }

  // ========================================================================
  // CREATEDISPATCH: Missing branch (line 32)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDispatchNullTokens() {
    CreateDispatch.fromTokens(null);
  }

  // ========================================================================
  // CREATEEVENTCOMMAND: Missing branches
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventCommandTokensGetOneNotEvent() {
    new CreateEventCommand(List.of("create", "notEvent", "Subject", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00")).execute(new StubManager());
  }

  @Test
  public void testCreateEventCommandFromToVariantWithoutRepeats() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    new CreateEventCommand(List.of("create", "event", "NoRepeat", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00")).execute(mgr);

    assertEquals(1, cal.events.size());
    assertTrue(outContent.toString().contains("Created single event"));
  }

  @Test
  public void testCreateEventCommandOnVariantWithoutRepeats() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    new CreateEventCommand(List.of("create", "event", "AllDay", "on",
        "2025-11-25")).execute(mgr);

    assertEquals(1, cal.events.size());
    assertTrue(outContent.toString().contains("Created all-day event"));
  }

  @Test
  public void testCreateEventCommandHandleRecurrenceWithFor() {
    StubManager mgr = new StubManager();
    new CreateEventCommand(List.of("create", "event", "Weekly", "on",
        "2025-11-25", "repeats", "MTWRF", "for", "5")).execute(mgr);

    assertTrue(outContent.toString().contains("5 times"));
  }

  @Test
  public void testCreateEventCommandParseOptionalFieldsDescriptionAtEnd() {
    StubManager mgr = new StubManager();
    new CreateEventCommand(List.of("create", "event", "Task", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00", "description"))
        .execute(mgr);

    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    assertTrue(cal.events.size() > 0);
  }

  @Test
  public void testCreateEventCommandParseOptionalFieldsLocationAtEnd() {
    StubManager mgr = new StubManager();
    new CreateEventCommand(List.of("create", "event", "Task", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00", "location"))
        .execute(mgr);

    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    assertTrue(cal.events.size() > 0);
  }

  @Test
  public void testCreateEventCommandParseSubjectLoopBoundary() {
    StubManager mgr = new StubManager();
    new CreateEventCommand(List.of("create", "event", "\"Final\"", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00")).execute(mgr);

    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    assertEquals("Final", cal.events.get(0).getSubject());
  }

  // ========================================================================
  // CSVEXPORTER: Missing lines (34-35)
  // ========================================================================

  @Test
  public void testCsvExporterWritesSuccessfully() {
    // The IOException path is actually very hard to trigger in real scenarios
    // This test ensures the normal path works, covering the try block
    CsvExporter exporter = new CsvExporter();
    ZonedDateTime now = ZonedDateTime.now();
    Event event = new Event.Builder("Test", now, now.plusHours(1)).build();

    java.nio.file.Path result = exporter.export(List.of(event), "test_export.csv");

    assertNotNull(result);
    assertTrue(result.toString().endsWith(".csv"));

    // Clean up
    try {
      java.nio.file.Files.deleteIfExists(result);
    } catch (Exception e) {
      // ignore cleanup errors
    }
  }

  // ========================================================================
  // EDITCALENDARCOMMAND: Missing branch (line 47)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarCommandPropertyWithoutValue() {
    new EditCalendarCommand(List.of("edit", "calendar", "--name", "Work",
        "--property", "timezone")).execute(new StubManager());
  }

  // ========================================================================
  // EDITEVENTCOMMAND: Missing branches (lines 38, 70, 78, 172, 175)
  // ========================================================================

  @Test
  public void testEditEventCommandScopeNull() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    ZonedDateTime now = ZonedDateTime.now();
    cal.events.add(new Event.Builder("Task", now, now.plusHours(1)).build());

    EditEventCommand cmd = new EditEventCommand(List.of("edit", "event", "Task",
        "from", "2025-11-25T10:00", "to", "2025-11-25T11:00", "with",
        "location", "Zoom"), null);
    cmd.execute(mgr);

    assertTrue(outContent.toString().contains("Edited single event"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandFromIdxEqualsNegativeOne() {
    new EditEventCommand(List.of("edit", "event", "Task", "to",
        "2025-11-25T11:00", "with", "location", "Zoom"), "event")
        .execute(new StubManager());
  }

  @Test
  public void testEditEventCommandFromPlusTwoEqualsSize() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    ZonedDateTime now = ZonedDateTime.now();
    cal.events.add(new Event.Builder("Task", now, now.plusHours(1)).build());

    new EditEventCommand(List.of("edit", "event", "Task", "from",
        "2025-11-25T10:00", "with", "location", "Zoom"), "event").execute(mgr);

    assertTrue(outContent.toString().contains("Edited"));
  }

  @Test
  public void testEditEventCommandFormatValueWithNull() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    ZonedDateTime now = ZonedDateTime.now();
    Event evt = new Event.Builder("Task", now, now.plusHours(1))
        .description(null).build();
    cal.events.add(evt);

    new EditEventCommand(List.of("edit", "event", "Task", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00", "with",
        "description", "New"), "event").execute(mgr);

    assertTrue(outContent.toString().contains("New"));
  }

  @Test
  public void testEditEventCommandFormatValueWithNonZonedDateTime() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    ZonedDateTime now = ZonedDateTime.now();
    cal.events.add(new Event.Builder("Task", now, now.plusHours(1)).build());

    new EditEventCommand(List.of("edit", "event", "Task", "from",
        "2025-11-25T10:00", "to", "2025-11-25T11:00", "with",
        "subject", "NewTask"), "event").execute(mgr);

    assertTrue(outContent.toString().contains("NewTask"));
  }

  // ========================================================================
  // EXPORTCALCOMMAND: Missing branch (line 38)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testExportCalCommandNullManager() {
    new ExportCalCommand(List.of("test.csv")).execute(null);
  }

  // ========================================================================
  // EXPORTDISPATCH: Missing branches (lines 29, 33)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testExportDispatchNullTokens() {
    ExportDispatch.fromTokens(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportDispatchEmptyTokens() {
    ExportDispatch.fromTokens(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportDispatchTwoTokensOnly() {
    ExportDispatch.fromTokens(List.of("export", "calendar"));
  }

  // ========================================================================
  // GUIFEATURESCONTROLLER: Missing lines (156, 158, 161, 162)
  // ========================================================================

  @Test
  public void testGuiFeaturesControllerUseCalendarSuccess() {
    FakeView view = new FakeView();
    IcommandAdapter adapter = new CommandAdapter();
    StubManager mgr = new StubManager();

    GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);

    controller.useCalendar("Personal");

    assertEquals("Personal", controller.getCurrentCalendarName());
  }

  // ========================================================================
  // ICALEXPORTER: Missing lines (72-73)
  // ========================================================================

  @Test
  public void testIcalExporterWritesSuccessfully() {
    // The IOException path is hard to trigger, test normal path
    IcalExporter exporter = new IcalExporter(ZoneId.of("America/New_York"));
    ZonedDateTime now = ZonedDateTime.now();
    Event event = new Event.Builder("Test", now, now.plusHours(1)).build();

    java.nio.file.Path result = exporter.export(List.of(event), "test_ical.ics");

    assertNotNull(result);
    assertTrue(result.toString().endsWith(".ics"));

    // Clean up
    try {
      java.nio.file.Files.deleteIfExists(result);
    } catch (Exception e) {
      // ignore cleanup errors
    }
  }

  // ========================================================================
  // PARSEUTILS: Missing branch (line 63, 70)
  // ========================================================================

  @Test
  public void testParseUtilsParseDateTimeEstFallbackPath() {
    ZonedDateTime result = ParseUtils.parseDateTimeEst("2025-11-25T10:00:00.000");
    assertNotNull(result);
    assertEquals(10, result.getHour());
  }

  // ========================================================================
  // PRINTDISPATCH: Missing branches (lines 28, 32, 39, 40)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchNullTokens() {
    PrintDispatch.fromTokens(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchEmptyTokens() {
    PrintDispatch.fromTokens(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchThreeTokensOnly() {
    PrintDispatch.fromTokens(List.of("print", "events", "on"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchFromWithoutTo() {
    PrintDispatch.fromTokens(List.of("print", "events", "from",
        "2025-11-25T10:00", "wrong", "2025-11-25T11:00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchFiveTokensWithoutProperKeywords() {
    PrintDispatch.fromTokens(List.of("print", "events", "wrong",
        "2025-11-25", "data"));
  }

  // ========================================================================
  // PRINTEVENTSONCOMMAND: Missing branch (line 25)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsOnCommandNullManager() {
    new PrintEventsOnCommand(List.of("2025-11-25")).execute(null);
  }

  // ========================================================================
  // PRINTEVENTSRANGECOMMAND: Missing branch (line 26)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsRangeCommandNullManager() {
    new PrintEventsRangeCommand(List.of("2025-11-25T10:00",
        "2025-11-25T11:00")).execute(null);
  }

  // ========================================================================
  // QUERYCOMMAND: Missing branches (lines 48, 93)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testQueryCommandArgsNull() {
    new QueryCommand(null).execute(new StubManager());
  }

  @Test
  public void testQueryCommandPrintEventsWithNullList() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();

    cal.events.clear();

    QueryCommand cmd = new QueryCommand(List.of("date", "2025-11-25"));
    cmd.execute(mgr);

    assertTrue(outContent.toString().contains("(no events found)"));
  }

  // ========================================================================
  // SHOWDISPATCH: Missing branches (lines 29, 34, 35)
  // ========================================================================

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchNullTokens() {
    ShowDispatch.fromTokens(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchEmptyTokens() {
    ShowDispatch.fromTokens(Collections.emptyList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchWrongSecondToken() {
    ShowDispatch.fromTokens(List.of("show", "wrong", "on", "2025-11-25T10:00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchWrongThirdToken() {
    ShowDispatch.fromTokens(List.of("show", "status", "wrong", "2025-11-25T10:00"));
  }

  // ========================================================================
  // COPYCOMMAND: Boundary mutations and method calls
  // ========================================================================

  @Test
  public void testCopyCommandSingleEventBoundaryConditions() {
    StubManager mgr = new StubManager();
    StubCalendar workCal = (StubCalendar) mgr.getCalendar("Work");
    workCal.events.clear();

    ZonedDateTime now = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    workCal.events.add(new Event.Builder("Meeting", now,
        now.plusHours(1)).build());

    new CopyCommand(List.of("copy", "event", "Meeting", "on",
        "2025-11-25T10:00", "--target", "Personal", "to",
        "2025-11-26T10:00")).execute(mgr);

    assertTrue(outContent.toString().contains("Copied event"));
  }

  @Test
  public void testCopyCommandEventsByDateBoundaryConditions() {
    StubManager mgr = new StubManager();
    StubCalendar workCal = (StubCalendar) mgr.getCalendar("Work");
    workCal.events.clear();

    ZonedDateTime now = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    workCal.events.add(new Event.Builder("Event", now, now.plusHours(1)).build());

    new CopyCommand(List.of("copy", "events", "on", "2025-11-25",
        "--target", "Personal", "to", "2025-11-26")).execute(mgr);

    assertTrue(outContent.toString().contains("Copied all events"));
  }

  @Test
  public void testCopyCommandEventsInRangeBoundaryConditions() {
    StubManager mgr = new StubManager();
    StubCalendar workCal = (StubCalendar) mgr.getCalendar("Work");
    workCal.events.clear();

    ZonedDateTime now = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    workCal.events.add(new Event.Builder("Event", now, now.plusHours(1)).build());

    new CopyCommand(List.of("copy", "events", "between", "2025-11-24",
        "and", "2025-11-26", "--target", "Personal", "to", "2025-12-01"))
        .execute(mgr);

    assertTrue(outContent.toString().contains("Copied events"));
  }
}
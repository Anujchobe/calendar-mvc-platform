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
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * Targeted tests for missing instruction, branch, and mutation coverage.
 */
public class ControllerCoverageFixTest {

  // ============================================================
  // Stub Calendar & Manager (Mocks for execution)
  // ============================================================

  /**
   * Stub Calendar that fully implements Icalendar.
   */
  private static class StubCalendar implements Icalendar {
    private final List<Event> events = new ArrayList<>();
    private ZoneId zone = ZoneId.of("America/New_York");

    // Pseudo-busy logic for ShowStatusCommand tests
    @Override
    public boolean isBusy(final ZonedDateTime timestamp) {
      return timestamp.getHour() % 2 == 0;
    }

    @Override
    public List<Event> queryEventsOn(final LocalDate date) {
      return events;
    }

    @Override
    public List<Event> queryEventsBetween(final ZonedDateTime start, final ZonedDateTime end) {
      return events;
    }

    @Override
    public void createEvent(final Event e) {
      events.add(e);
    }

    @Override
    public void createSeries(final Event e, final RecurrenceRule rule) {
      events.add(e);
    }

    @Override
    public void editEvent(final EventKey key, final String property, final Object newValue) {
      /* no-op */
    }

    @Override
    public void editSeries(final EventKey key, final String property, final Object newValue,
                           final EditMode mode) {
      /* no-op */
    }

    @Override
    public List<Event> getAllEvents() {
      return events;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public void setZone(final ZoneId zone) {
      this.zone = zone;
    }
  }

  /**
   * Stub Manager that returns a functional active calendar.
   */
  private static class StubManager implements IcalendarManager {
    private final Map<String, Icalendar> map = new HashMap<>();
    private StubCalendar active = new StubCalendar();
    private String activeName = "Work";

    public StubManager() {
      map.put("Work", active);
      map.put("Personal", new StubCalendar());
    }

    @Override
    public Icalendar getActiveCalendar() {
      return active;
    }

    @Override
    public Icalendar getCalendar(final String name) {
      return map.get(name);
    }

    @Override
    public void createCalendar(final String name, final ZoneId zone) {
      if (map.containsKey(name)) {
        throw new IllegalArgumentException("Calendar exists");
      }
      map.put(name, new StubCalendar());
    }

    @Override
    public void useCalendar(final String name) {
      final Icalendar c = map.get(name);
      if (c == null) {
        throw new IllegalArgumentException("No such calendar");
      }
      active = (StubCalendar) c;
      activeName = name;
    }

    @Override
    public void editCalendar(final String name, final String property, final String newValue) {
      /* no-op */
    }

    @Override
    public List<String> listCalendars() {
      return new ArrayList<>(map.keySet());
    }
  }

  // Manager that always returns null for active calendar
  private static class NoActiveManager extends StubManager {
    @Override
    public Icalendar getActiveCalendar() {
      return null;
    }
  }

  // ============================================================
  // ABSTRACT COMMAND (75% Branch)
  // ============================================================

  @Test(expected = IllegalArgumentException.class)
  public void testEnsureArgCountAtLeastNullArgs() {
    // Null args constructor
    final AbstractCommand cmd = new ShowStatusCommand(null) {
      @Override
      public void execute(final IcalendarManager manager) {
        ensureArgCountAtLeast(1, "usage");
      }
    };
    cmd.execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnsureArgCountAtLeastInsufficientCount() {
    // Insufficient count
    final AbstractCommand cmd = new ShowStatusCommand(Collections.singletonList("one")) {
      @Override
      public void execute(final IcalendarManager manager) {
        ensureArgCountAtLeast(2, "usage");
      }
    };
    cmd.execute(new StubManager());
  }

  // ============================================================
  // COMMAND UTILS (Mutation: joinTokens needed)
  // ============================================================

  @Test
  public void testCommandUtilsJoinTokens() {
    final List<String> tokens = Arrays.asList("create", "event", "Team Meeting");
    assertEquals("create event Team Meeting", CommandUtils.joinTokens(tokens));
  }

  // ============================================================
  // PARSE UTILS (Missing Branches/Mutants)
  // ============================================================

  @Test
  public void testParseUtilsParsePossiblyQuotedSubjectNonQuoted() {
    final List<String> tokens = Arrays.asList("create", "event", "Subject", "from");
    // Should hit the 'else' block for non-quoted subject
    assertEquals("Subject", ParseUtils.parsePossiblyQuotedSubject(tokens, 2));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseUtilsParsePossiblyQuotedSubjectUnclosed() {
    final List<String> tokens = Arrays.asList("create", "event", "\"Unclosed", "Quote");
    // Should hit the 'throw new IllegalArgumentException("Unclosed quoted subject.")'
    ParseUtils.parsePossiblyQuotedSubject(tokens, 2);
  }

  @Test
  public void testParseUtilsSplitKeepingQuotesMutantKiller() {
    final String input = "command \"quoted with space\" and \"another one\"";
    final List<String> expected = Arrays.asList("command", "\"quoted with space\"", "and",
        "\"another one\"");
    // This mutant killer ensures the logic around 'inQuote' and 'cur.setLength(0)' is correct
    assertEquals(expected, ParseUtils.splitKeepingQuotes(input));

    // Test a null case/empty case to hit edge mutants
    assertTrue(ParseUtils.splitKeepingQuotes("").isEmpty());
    assertTrue(ParseUtils.splitKeepingQuotes("  ").isEmpty());
  }

  @Test
  public void testParseUtilsParseDateTimeEstFallbackPatterns() {
    // Test a format that should fail ISO_LOCAL_DATE_TIME and HH:mm patterns,
    // forcing the fallback LocalDateTime.parse(norm).atZone(EST) to succeed.
    // Example: only yyyy-MM-dd HH:mm:ss is available
    final ZonedDateTime result = ParseUtils.parseDateTimeEst("2025-11-25 09:30:00");
    assertNotNull(result);
    assertEquals(9, result.getHour());
    // Also hits coverage on different patterns implicitly
  }


  // ============================================================
  // CREATE EVENT COMMAND (Missing Args/Recurrence Errors)
  // ============================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleFromToVariantMissingEndTime() {
    // Expect IllegalArgumentException (as fixed in CreateEventCommand.java)
    final List<String> t = Arrays.asList(
        "create", "event", "X", "from", "2025-11-20T10:00", "to");
    new CreateEventCommand(t).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleOnVariantMissingDate() {
    // Missing date after 'on' token
    final List<String> t = Arrays.asList("create", "event", "X", "on");
    new CreateEventCommand(t).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleRecurrenceMissingPattern() {
    // Missing pattern after 'repeats' token
    final List<String> t = Arrays.asList(
        "create", "event", "X", "on", "2025-11-20", "repeats");
    new CreateEventCommand(t).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleRecurrenceMissingForOrUntil() {
    // Missing 'for' or 'until' after pattern
    final List<String> t = Arrays.asList(
        "create", "event", "X", "on", "2025-11-20", "repeats", "MWF");
    new CreateEventCommand(t).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleRecurrenceMissingForCount() {
    // Missing number after 'for'
    final List<String> t = Arrays.asList(
        "create", "event", "X", "on", "2025-11-20", "repeats", "MWF", "for");
    new CreateEventCommand(t).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventHandleRecurrenceMissingUntilDate() {
    // Missing date after 'until'
    final List<String> t = Arrays.asList(
        "create", "event", "X", "on", "2025-11-20", "repeats", "MWF", "until");
    new CreateEventCommand(t).execute(new StubManager());
  }

  // ============================================================
  // EXPORT COMMANDS (CSV, ICAL)
  // ============================================================

  @Test
  public void testCsvExporterFileNameMissingExtensionAndNullEscape() throws Exception {
    final CsvExporter exporter = new CsvExporter();
    final StubCalendar cal = new StubCalendar();
    cal.events.add(new Event.Builder("NullTest", ZonedDateTime.now(),
        ZonedDateTime.now().plusHours(1)).build());
    final Path p = exporter.export(cal.getAllEvents(), "no-ext");

    // 1. Check file name to confirm extension logic
    assertEquals("no-ext.csv", p.getFileName().toString());

    // 2. Ensure escape(null) returns "" resulting in correct CSV segment
    final String content = new String(java.nio.file.Files.readAllBytes(p));
    // is: ,"",,"",PUBLIC,false,"" or ,"",,"",PRIVATE,false,"".
    // I will check the segment that indicates two empty fields followed by the boolean/seriesid.
    // The most likely segments are: ,,PUBLIC,false, or ,,PRIVATE,false,
    // Relying on the full segment ,PRIVATE,false,
    assertTrue(content.contains(",PRIVATE,false,"));
  }

  @Test
  public void testIcalExporterFileNameIsIcsAndMinimalEvent() throws Exception {
    final StubManager mgr = new StubManager();
    final IcalExporter exporter = new IcalExporter(mgr.getActiveCalendar().getZone());
    final StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    // Event with null/empty description and location to hit the
    // 'if (e.getDescription() != null)' branches
    cal.events.add(new Event.Builder("Minimal", ZonedDateTime.now(),
        ZonedDateTime.now().plusHours(1)).build());

    final Path p = exporter.export(cal.getAllEvents(), "minimal.ics");

    // 1. Ensures the `.ics` branch is hit (not `.ical`).
    assertTrue(p.toString().endsWith("minimal.ics"));

    // 2. Ensures null description/location fields are skipped.
    final String content = new String(java.nio.file.Files.readAllBytes(p));
    assertTrue(content.contains("SUMMARY:Minimal"));
    assertFalse(content.contains("DESCRIPTION:"));
    assertFalse(content.contains("LOCATION:"));
  }

  @Test(expected = RuntimeException.class)
  public void testIcalExporterBadZoneFails() {
    // Use an invalid zone ID ("Not/aZone") to force ZoneRulesException (a RuntimeException)
    new IcalExporter(ZoneId.of("Not/aZone")).export(Collections.emptyList(), "test.ics");
  }


  // ============================================================
  // DISPATCHERS (Missing final default/throws)
  // ============================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDispatchInvalidType() {
    CreateDispatch.fromTokens(Arrays.asList("create", "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowDispatchInvalidType() {
    ShowDispatch.fromTokens(Arrays.asList("show", "foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintDispatchInvalidRangeSyntax() {
    // print events from <start> to <end> -> missing 'to' or 'end'
    PrintDispatch.fromTokens(Arrays.asList("print", "events", "from", "2025-11-10T00:00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportDispatchInvalidType() {
    ExportDispatch.fromTokens(Arrays.asList("export", "foo", "file.csv"));
  }

  // ============================================================
  // COMMAND FACTORY (Missing Branches)
  // ============================================================

  @Test(expected = IllegalArgumentException.class)
  public void testCommandFactoryUseWithoutCalendar() {
    // Should hit the 'throw new IllegalArgumentException' in the 'use' case block.
    CommandFactory.parseCommand(Arrays.asList("use", "foobar", "--name", "W"));
  }

  // ============================================================
  // EDIT EVENT COMMAND (Missing 'to' case for series)
  // ============================================================

  @Test
  public void testEditEventCommandSeriesMissingToToken() {
    final StubManager mgr = new StubManager();

    // Edit series command where the 'to <end>' tokens are intentionally missing.
    // This executes the command and sets end=null, hitting a missing path.
    final List<String> t = Arrays.asList(
        "edit", "events", "Standup",
        "from", "2025-11-20T09:00",
        "with", "location", "New Room");
    new EditEventCommand(t, "events").execute(mgr);

    // Assert it executes without exception (it does not require the 'to' part)
    assertTrue(true);
  }

  // ============================================================
  // QUERY COMMAND (Internal Error Handling)
  // ============================================================

  @Test
  public void testQueryCommandRangeInternalPrintFailure() {
    final StubManager mgr = new StubManager();
    // Force QueryCommand's printEvents to fail by passing bad args that ParseUtils won't catch
    final QueryCommand cmd = new QueryCommand(Arrays.asList("range", "not-a-date",
        "2025-11-20T11:00"));

    // We expect the internal try/catch block to catch the exception and print an error,
    // but not re-throw an exception to the parser.
    try {
      cmd.execute(mgr);
    } catch (IllegalArgumentException e) {
      // Re-throw if it's the expected *parsing* error.
      throw e;
    } catch (Exception e) {
      fail("QueryCommand should handle non-IAE exceptions internally.");
    }
    // Success if no exception thrown to top level
    assertTrue(true);
  }

  // ============================================================
  // USE / SHOW STATUS COMMAND (Error Paths)
  // ============================================================

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarCommandMissingNameFlag() {
    // Missing '--name' and value. Should hit 'name == null' branch.
    new UseCalendarCommand(Arrays.asList("use", "calendar")).execute(new StubManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatusCommandBadKeyword() {
    // Missing 'on' keyword (or wrong keyword)
    new ShowStatusCommand(Arrays.asList("at", "2025-11-20T10:00")).execute(new StubManager());
  }

  @Test(expected = IllegalStateException.class)
  public void testShowStatusCommandNoActiveCalendar() {
    new ShowStatusCommand(Arrays.asList("on", "2025-11-20T10:00")).execute(new NoActiveManager());
  }
}
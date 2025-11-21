package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.mocks.FakeCalendar;
import calendar.controller.mocks.FakeManager;
import calendar.model.Event;
import calendar.model.IcalendarManager;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.Test;

/**
 * Tests for edge cases and error handling in the controller.
 */
public class ControllerEdgeAndErrorTest {

  @Test(expected = IllegalArgumentException.class)
  public void testCommandFactoryUnknownCommandThrows() {
    CommandFactory.parseCommand(CommandUtils.tokenize("fooble dooble"));
  }

  @Test
  public void testUseCalendarMissingNameErrorsViaParser() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();
    new CommandParser(m, new StringReader("use calendar --name"), out).run();
    assertTrue(out.toString().contains("Error:"));
  }

  @Test
  public void testCreateCalendarInvalidTimezoneErrors() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();
    new CommandParser(m,
        new StringReader("create calendar --name Bad --timezone Not/AZone"), out).run();
    assertTrue(out.toString().toLowerCase().contains("invalid timezone"));
  }

  @Test
  public void testExportUnsupportedExtensionErrors() {
    IcalendarManager m = new FakeManager();
    ((FakeCalendar) m.getCalendar("Work")).events.add(
        new Event.Builder("E",
            ParseUtils.parseDateTimeEst("2025-11-10T08:00"),
            ParseUtils.parseDateTimeEst("2025-11-10T09:00")).build());
    StringBuilder out = new StringBuilder();
    new CommandParser(m, new StringReader("export calendar bad.txt"), out).run();
    assertTrue(out.toString().toLowerCase().contains("unsupported"));
  }

  @Test
  public void testPrintDispatchBadSyntaxErrors() {
    try {
      PrintDispatch.fromTokens(CommandUtils.tokenize("print"));
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Usage: print events"));
    }
  }

  @Test
  public void testPrintOnAndRangeUsageErrorsThroughCommands() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();

    // Case 1: missing date after "on"
    new CommandParser(m, new StringReader("use calendar --name Work\nprint events on"), out).run();
    String msg1 = out.toString().toLowerCase();
    assertTrue(msg1.contains("error") && msg1.contains("print events on"));

    // Case 2: incomplete range
    out.setLength(0);
    new CommandParser(m,
        new StringReader("use calendar --name Work\nprint events from 2025-11-10T00:00 to"), out)
        .run();
    String msg2 = out.toString().toLowerCase();
    assertTrue(msg2.contains("error") && msg2.contains("print events from"));
  }

  @Test
  public void testEditEventMissingFromAndMissingWithErrors() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();
    new CommandParser(m,
        new StringReader("use calendar --name Work\nedit event \"S\" "
            + "2025-11-10T10:00 to 2025-11-10T11:00 with location L"),
        out).run();
    assertTrue(out.toString().contains("Missing 'from'"));

    out.setLength(0);
    new CommandParser(m,
        new StringReader("use calendar --name Work\nedit event \"S\" "
            + "from 2025-11-10T10:00 to 2025-11-10T11:00"),
        out).run();
    assertTrue(out.toString().contains("Missing 'with"));
  }

  @Test
  public void testEditEventInvalidPropertyErrors() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();
    new CommandParser(m,
        new StringReader("use calendar --name Work\nedit event \"S\" from 2025-11-10T10:00 "
            + "to 2025-11-10T11:00 with nonsense value"),
        out).run();
    assertTrue(out.toString().contains("Unknown property"));
  }

  @Test
  public void testCopyCommandSyntaxErrorsAllThreeFlavors() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();

    new CommandParser(m, new StringReader(
        "use calendar --name Work\ncopy event \"A\""), out).run();
    assertTrue(out.toString().contains("Usage: copy event"));

    out.setLength(0);
    new CommandParser(m,
        new StringReader("use calendar --name Work\ncopy events on 2025-11-10 --target"), out)
        .run();
    assertTrue(out.toString().toLowerCase().contains("missing target"));

    out.setLength(0);
    new CommandParser(m,
        new StringReader("use calendar --name Work\ncopy events between "
            + "2025-11-10 and 2025-11-11 --target Personal"),
        out).run();
    assertTrue(out.toString().toLowerCase().contains("usage: copy events between"));
  }

  @Test
  public void testParseUtilsWeekdaysAndSplitKeepingQuotesAndDateTimeParsing() {
    Set<DayOfWeek> set = ParseUtils.parseWeekdays("MTWRF");
    assertEquals(5, set.size());
    try {
      ParseUtils.parseWeekdays("MX");
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().toLowerCase().contains("invalid weekday"));
    }
    assertEquals(5, ParseUtils.splitKeepingQuotes(
        "create event \"Team Meeting\" from 2025-11-10T10:00").size());
    ZonedDateTime a = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    ZonedDateTime b = ParseUtils.parseDateTimeEst("2025-11-10 10:00");
    ZonedDateTime c = ParseUtils.parseDateTimeEst("2025-11-10T10:00:00");
    assertNotNull(a);
    assertNotNull(b);
    assertNotNull(c);
  }

  @Test
  public void testCreateDispatchUsageError() {
    try {
      CreateDispatch.fromTokens(CommandUtils.tokenize("create"));
      fail("Expected IAE");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().toLowerCase().contains("usage: create event"));
    }
  }

  @Test
  public void testCommandParserProducesErrorLinesForBadCommands() {
    IcalendarManager m = new FakeManager();
    StringBuilder out = new StringBuilder();
    String script =
        "use calendar --name Work\n"
            + "print\n"
            + "export calendar bad.ext\n"
            + "edit series \"X\" with location Zoom\n";
    new CommandParser(m, new StringReader(script), out).run();

    String log = out.toString();
    assertTrue(log.contains("Error:"));
    assertTrue(log.split("Error:").length >= 3);
  }
}

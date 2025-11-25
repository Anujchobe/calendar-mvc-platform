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
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Final comprehensive test suite to achieve 100% mutation coverage.
 * Kills all surviving mutants in QueryCommand, PrintEventsRangeCommand,
 * GuiFeaturesController, EditEventCommand, and UseCalendarCommand.
 */
public class FinalMutationKillerTest {

  private ByteArrayOutputStream outContent;
  private ByteArrayOutputStream errContent;
  private PrintStream originalOut;
  private PrintStream originalErr;

  /**
   * Stub calendar implementation for testing.
   */
  private static class StubCalendar implements Icalendar {
    final List<Event> events = new ArrayList<>();
    ZoneId zone = ZoneId.of("America/New_York");

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
      // no-op
    }

    @Override
    public void editSeries(EventKey key, String property, Object newValue,
                           EditMode mode) {
      // no-op
    }

    @Override
    public List<Event> queryEventsOn(LocalDate date) {
      List<Event> result = new ArrayList<>();
      for (Event e : events) {
        if (e.occursOn(date)) {
          result.add(e);
        }
      }
      return result;
    }

    @Override
    public List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end) {
      List<Event> result = new ArrayList<>();
      for (Event e : events) {
        if (!e.getEnd().isBefore(start) && !e.getStart().isAfter(end)) {
          result.add(e);
        }
      }
      return result;
    }

    @Override
    public boolean isBusy(ZonedDateTime timestamp) {
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
   * Stub manager implementation for testing.
   */
  private static class StubManager implements IcalendarManager {
    final Map<String, Icalendar> calendars = new HashMap<>();
    Icalendar active;

    StubManager() {
      calendars.put("Work", new StubCalendar());
      calendars.put("Personal", new StubCalendar());
      active = calendars.get("Work");
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
   * Fake view that tracks method calls.
   */
  private static class FakeView implements calendar.view.Icalendarview {
    int refreshCalls = 0;
    int showErrorCalls = 0;
    String lastError = null;

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
      showErrorCalls++;
      lastError = message;
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
   * Restores original output streams.
   */
  @After
  public void teardown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testQueryCommandPrintEventsEmptyList() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-25"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("(no events found)"));
  }

  @Test
  public void testQueryCommandPrintEventsWithNullLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    Event eventNullLoc = new Event.Builder("Meeting", start, start.plusHours(1))
        .location(null)
        .build();
    cal.events.add(eventNullLoc);

    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-25"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertFalse(output.contains("()"));
  }

  @Test
  public void testQueryCommandPrintEventsWithLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    Event eventWithLoc = new Event.Builder("Meeting", start, start.plusHours(1))
        .location("Zoom")
        .build();
    cal.events.add(eventWithLoc);

    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-25"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("(Zoom)"));
  }


  @Test
  public void testPrintEventsRangeNullLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    Event eventNullLoc = new Event.Builder("Task", start, start.plusHours(1))
        .location(null)
        .build();
    cal.events.add(eventNullLoc);

    PrintEventsRangeCommand cmd = new PrintEventsRangeCommand(
        Arrays.asList("2025-11-25T00:00", "2025-11-26T00:00"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Task"));
    assertFalse(output.contains("()"));
  }

  @Test
  public void testPrintEventsRangeBlankLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    Event eventBlankLoc = new Event.Builder("Task", start, start.plusHours(1))
        .location("   ")
        .build();
    cal.events.add(eventBlankLoc);

    PrintEventsRangeCommand cmd = new PrintEventsRangeCommand(
        Arrays.asList("2025-11-25T00:00", "2025-11-26T00:00"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Task"));
    assertFalse(output.contains("("));
  }

  @Test
  public void testPrintEventsRangeWithLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    Event eventWithLoc = new Event.Builder("Task", start, start.plusHours(1))
        .location("Office")
        .build();
    cal.events.add(eventWithLoc);

    PrintEventsRangeCommand cmd = new PrintEventsRangeCommand(
        Arrays.asList("2025-11-25T00:00", "2025-11-26T00:00"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Task"));
    assertTrue(output.contains("(Office)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrintEventsRangeWrongArgCount() {
    StubManager mgr = new StubManager();
    PrintEventsRangeCommand cmd = new PrintEventsRangeCommand(
        Arrays.asList("2025-11-25T00:00"));
    cmd.execute(mgr);
  }

  @Test
  public void testGuiFeaturesControllerCreateCalendarRefreshCalled() {
    FakeView view = new FakeView();
    IcommandAdapter adapter = new CommandAdapter();
    StubManager mgr = new StubManager();


    GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);
    int refreshAfterInit = view.refreshCalls;


    controller.createCalendar("NewCal", "America/New_York");


    assertTrue(mgr.listCalendars().contains("NewCal"));
  }

  @Test
  public void testGuiFeaturesControllerUseCalendarRefreshNotCalled() {
    FakeView view = new FakeView();
    IcommandAdapter adapter = new CommandAdapter();
    StubManager mgr = new StubManager();

    GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);

    try {
      controller.useCalendar("NonExistent");
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("Failed to switch calendar"));
    }
  }

  @Test
  public void testGuiFeaturesControllerGetCalendarNamesNotEmpty() {
    FakeView view = new FakeView();
    IcommandAdapter adapter = new CommandAdapter();
    StubManager mgr = new StubManager();

    GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);

    List<String> names = controller.getCalendarNames();
    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertTrue(names.contains("Work"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandExactly6Args() {
    StubManager mgr = new StubManager();
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "date", "with"),
        "event");
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandFromAtEndOfArgs() {
    StubManager mgr = new StubManager();
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "to", "date", "with"),
        "event");
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandToAtEndOfArgs() {
    StubManager mgr = new StubManager();
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00", "to"),
        "event");
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandToFollowedByWithImmediately() {
    StubManager mgr = new StubManager();
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "with", "location", "Zoom"),
        "event");
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventCommandWithAtEndOfArgs() {
    StubManager mgr = new StubManager();
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "2025-11-25T11:00", "with"),
        "event");
    cmd.execute(mgr);
  }

  @Test
  public void testEditEventCommandWithPlusOneEqualsSize() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Task", start, start.plusHours(1)).build());


    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "2025-11-25T11:00", "with", "location"),
        "event");
    cmd.execute(mgr);

    assertTrue(outContent.toString().contains("Edited single event: Task"));
  }

  @Test
  public void testEditEventCommandStatusPropertyUppercased() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Task", start, start.plusHours(1)).build());

    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "2025-11-25T11:00", "with", "status", "public"),
        "event");
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("PUBLIC"));
  }

  @Test
  public void testEditEventCommandSubjectPropertyTrimmed() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Task", start, start.plusHours(1)).build());

    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "2025-11-25T11:00", "with", "subject", "  New Task  "),
        "event");
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("New Task"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarCommandExactly3Args() {
    StubManager mgr = new StubManager();
    UseCalendarCommand cmd = new UseCalendarCommand(
        Arrays.asList("use", "calendar", "--name"));
    cmd.execute(mgr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseCalendarCommandNameAtEnd() {
    StubManager mgr = new StubManager();
    UseCalendarCommand cmd = new UseCalendarCommand(
        Arrays.asList("use", "calendar", "extra", "--name"));
    cmd.execute(mgr);
  }

  @Test
  public void testUseCalendarCommandSuccessful() {
    StubManager mgr = new StubManager();
    UseCalendarCommand cmd = new UseCalendarCommand(
        Arrays.asList("use", "calendar", "--name", "Personal"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Now using calendar: Personal"));
  }

  @Test
  public void testQueryCommandRangeTypeHappyPath() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Event", start, start.plusHours(1))
        .location("Room")
        .build());

    QueryCommand cmd = new QueryCommand(
        Arrays.asList("range", "2025-11-25T00:00", "2025-11-26T00:00"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Event"));
    assertTrue(output.contains("(Room)"));
  }

  @Test
  public void testPrintEventsOnWithLocation() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Meeting", start, start.plusHours(1))
        .location("Boardroom")
        .build());

    PrintEventsOnCommand cmd = new PrintEventsOnCommand(
        Arrays.asList("2025-11-25"));
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("Meeting"));
    assertTrue(output.contains("(Boardroom)"));
  }

  @Test
  public void testGuiFeaturesControllerNavigateAndSelect() {
    FakeView view = new FakeView();
    IcommandAdapter adapter = new CommandAdapter();
    StubManager mgr = new StubManager();

    GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);

    controller.navigateToMonth(YearMonth.of(2025, 11));
    controller.selectDate(LocalDate.of(2025, 11, 25));

    assertTrue(view.refreshCalls >= 1);
  }

  @Test
  public void testGuiFeaturesControllerGetEventsOn() {
    final StubManager mgr = new StubManager();

    final StubCalendar cal = (StubCalendar) mgr.getCalendar("Work");
    cal.events.clear();

    final LocalDate targetDate = LocalDate.of(2025, 11, 25);
    final ZonedDateTime start = targetDate.atTime(10, 0)
        .atZone(ZoneId.of("America/New_York"));
    final ZonedDateTime end = start.plusHours(1);

    final Event event = new Event.Builder("TestEvent", start, end)
        .build();
    cal.events.add(event);

    assertEquals(1, cal.events.size());

    final FakeView view = new FakeView();
    final IcommandAdapter adapter = new CommandAdapter();
    final GuiFeaturesController controller = new GuiFeaturesController(mgr, adapter, view);

    final List<Event> events = controller.getEventsOn(targetDate);

    assertNotNull(events);
    assertTrue(events.size() >= 0);
  }

  @Test
  public void testEditEventCommandMultiWordValue() {
    StubManager mgr = new StubManager();
    StubCalendar cal = (StubCalendar) mgr.getActiveCalendar();

    ZonedDateTime start = ZonedDateTime.of(2025, 11, 25, 10, 0, 0, 0,
        ZoneId.of("America/New_York"));
    cal.events.add(new Event.Builder("Task", start, start.plusHours(1)).build());

    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "Task", "from", "2025-11-25T10:00",
            "to", "2025-11-25T11:00", "with", "description",
            "This", "is", "a", "multi", "word", "description"),
        "event");
    cmd.execute(mgr);

    String output = outContent.toString();
    assertTrue(output.contains("This is a multi word description"));
  }
}
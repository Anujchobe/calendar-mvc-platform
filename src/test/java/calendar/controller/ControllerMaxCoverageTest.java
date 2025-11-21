package calendar.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import calendar.controller.mocks.FakeCalendar;
import calendar.controller.mocks.FakeManager;
import calendar.model.EditMode;
import calendar.model.Event;
import calendar.model.EventStatus;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * Extra coverage & mutation-killing tests for controller commands.
 * Focus: CreateEventCommand, QueryCommand, CopyCommand,
 * EditEventCommand, ShowStatusCommand.
 */
public class ControllerMaxCoverageTest {

  // ===== Helpers =====

  /**
   * Manager whose active calendar is always null (to hit "no active calendar" branches).
   */
  private static class NoActiveManager extends FakeManager {
    @Override
    public Icalendar getActiveCalendar() {
      return null;
    }
  }

  // ----------------------------------------------------------
  // CreateEventCommand – success + all major error branches
  // ----------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventManagerNull() {
    List<String> tokens = Arrays.asList("create", "event", "\"X\"", "from",
        "2025-11-10T10:00", "to", "2025-11-10T11:00");
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testCreateEventNoActiveCalendar() {
    List<String> tokens = Arrays.asList("create", "event", "\"X\"", "from",
        "2025-11-10T10:00", "to", "2025-11-10T11:00");
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    IcalendarManager m = new NoActiveManager();
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventTooFewTokens() {
    List<String> tokens = Arrays.asList("create", "event", "\"X\"");
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventInvalidPrefix() {
    List<String> tokens = Arrays.asList(
        "foo", "bar", "\"X\"", "from", "2025-11-10T10:00", "to", "2025-11-10T11:00");
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventMissingFromAndOnKeyword() {
    List<String> tokens = Arrays.asList("create", "event", "\"X\"", "blah");
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(new FakeManager());
  }

  @Test
  public void testCreateSingleEventFromToWithOptionalFields() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();

    List<String> tokens = Arrays.asList(
        "create", "event", "\"Meeting\"",
        "from", "2025-11-10T10:00",
        "to", "2025-11-10T11:00",
        "description", "\"Weekly\"",
        "location", "\"Zoom\"",
        "status", "PUBLIC"
    );

    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);

    assertEquals(1, cal.events.size());
    Event e = cal.events.get(0);
    assertEquals("Meeting", e.getSubject());
    assertEquals("Weekly", e.getDescription());
    assertEquals("Zoom", e.getLocation());
    assertEquals(EventStatus.PUBLIC, e.getStatus());
  }

  @Test
  public void testCreateSingleEventFromToWithRepeatsFor() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();
    cal.calls.clear();

    List<String> tokens = Arrays.asList(
        "create", "event", "\"Class\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "repeats", "MTWRF",
        "for", "3"
    );

    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);

    // Should create a series (not just single event)
    assertTrue(cal.calls.stream().anyMatch(s -> s.equals("series")));
  }

  @Test
  public void testCreateAllDayEventOnWithRepeatsUntil() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();
    cal.calls.clear();

    List<String> tokens = Arrays.asList(
        "create", "event", "\"Yoga\"",
        "on", "2025-11-11",
        "repeats", "MTWRF",
        "until", "2025-11-14"
    );

    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);

    assertTrue(cal.calls.stream().anyMatch(s -> s.equals("series")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventInvalidStatusToken() {
    FakeManager m = new FakeManager();
    List<String> tokens = Arrays.asList(
        "create", "event", "\"Meeting\"",
        "from", "2025-11-10T10:00",
        "to", "2025-11-10T11:00",
        "status", "GIBBERISH"
    );
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventRepeatsWithoutForOrUntil() {
    FakeManager m = new FakeManager();
    List<String> tokens = Arrays.asList(
        "create", "event", "\"X\"",
        "from", "2025-11-10T10:00",
        "to", "2025-11-10T11:00",
        "repeats", "MTWRF"
    );
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventRepeatsInvalidWeekday() {
    FakeManager m = new FakeManager();
    List<String> tokens = Arrays.asList(
        "create", "event", "\"X\"",
        "from", "2025-11-10T10:00",
        "to", "2025-11-10T11:00",
        "repeats", "MX",
        "for", "2"
    );
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventFromVariantInvalidDateTime() {
    FakeManager m = new FakeManager();
    List<String> tokens = Arrays.asList(
        "create", "event", "\"X\"",
        "from", "not-a-date",
        "to", "2025-11-10T11:00"
    );
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventOnVariantInvalidDate() {
    FakeManager m = new FakeManager();
    List<String> tokens = Arrays.asList(
        "create", "event", "\"X\"",
        "on", "2025/11/10" // bad format
    );
    CreateEventCommand cmd = new CreateEventCommand(tokens);
    cmd.execute(m);
  }

  // ----------------------------------------------------------
  // QueryCommand – all branches: date, range, unknown, bad args
  // ----------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testQueryManagerNull() {
    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-10"));
    cmd.execute(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testQueryNoActiveCalendar() {
    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-10"));
    cmd.execute(new NoActiveManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testQueryTooFewArgs() {
    QueryCommand cmd = new QueryCommand(Collections.singletonList("date"));
    cmd.execute(new FakeManager());
  }

  @Test
  public void testQueryDateHappyPath() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();

    // Add one event on a given date
    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T11:00");
    cal.events.add(new Event.Builder("Event1", s, e).build());

    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "2025-11-10"));
    // We can't see stdout here, but just ensure it doesn't throw
    cmd.execute(m);
  }

  @Test
  public void testQueryRangeHappyPath() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T11:00");
    cal.events.add(new Event.Builder("Event1", s, e).build());

    QueryCommand cmd = new QueryCommand(Arrays.asList(
        "range",
        "2025-11-10T00:00",
        "2025-11-11T00:00"
    ));
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testQueryRangeTooFewArgs() {
    QueryCommand cmd = new QueryCommand(Arrays.asList("range", "2025-11-10T00:00"));
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testQueryUnknownType() {
    QueryCommand cmd = new QueryCommand(Arrays.asList("foo", "2025-11-10"));
    cmd.execute(new FakeManager());
  }

  @Test
  public void testQueryDateInvalidFormat() {
    FakeManager m = new FakeManager();
    QueryCommand cmd = new QueryCommand(Arrays.asList("date", "not-a-date"));

    // Should NOT throw — QueryCommand swallows most parse errors
    try {
      cmd.execute(m);
    } catch (Exception e) {
      fail("QueryCommand should NOT throw on invalid date; got: " + e);
    }
  }


  // ----------------------------------------------------------
  // CopyCommand – event, events on, between, and all error paths
  // ----------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testCopyManagerNull() {
    CopyCommand cmd = new CopyCommand(Arrays.asList("copy", "event"));
    cmd.execute(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyTooFewArgs() {
    CopyCommand cmd = new CopyCommand(Arrays.asList("copy", "event", "\"A\""));
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalStateException.class)
  public void testCopyNoActiveCalendar() {
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "event", "\"A\"", "on", "2025-11-10T10:00",
        "--target", "Work", "to", "2025-11-10T11:00"));
    cmd.execute(new NoActiveManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyMissingTargetFlag() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "event", "\"A\"", "on", "2025-11-10T10:00", "to", "2025-11-10T11:00"));
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyTargetCalendarNotFound() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "event", "\"A\"", "on", "2025-11-10T10:00",
        "--target", "DoesNotExist", "to", "2025-11-10T11:00"));
    cmd.execute(m);
  }

  @Test
  public void testCopySingleEventHappyPath() {
    FakeManager m = new FakeManager();
    Icalendar work = m.getCalendar("Work");
    Icalendar personal = m.getCalendar("Personal");

    // Seed source calendar with event "A"
    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T11:00");
    work.createEvent(new Event.Builder("A", s, e).build());

    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "event", "\"A\"",
        "on", "2025-11-10T10:00",
        "--target", "Personal",
        "to", "2025-11-12T10:00"
    ));
    cmd.execute(m);

    assertTrue(((FakeCalendar) personal).events.size() >= 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopySingleEventBadDate() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "event", "\"A\"",
        "on", "bad-date",
        "--target", "Personal",
        "to", "2025-11-12T10:00"
    ));
    cmd.execute(m);
  }

  @Test
  public void testCopyEventsOnDateHappyPath() {
    FakeManager m = new FakeManager();
    Icalendar work = m.getCalendar("Work");
    Icalendar personal = m.getCalendar("Personal");

    ((FakeCalendar) work).events.clear();
    ((FakeCalendar) personal).events.clear();

    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T09:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    work.createEvent(new Event.Builder("B", s, e).build());

    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "events",
        "on", "2025-11-10",
        "--target", "Personal",
        "to", "2025-11-20"
    ));
    cmd.execute(m);

    assertTrue(((FakeCalendar) personal).events.size() >= 1);
  }

  @Test
  public void testCopyEventsBetweenDatesHappyPath() {
    FakeManager m = new FakeManager();
    Icalendar work = m.getCalendar("Work");
    Icalendar personal = m.getCalendar("Personal");

    ((FakeCalendar) work).events.clear();
    ((FakeCalendar) personal).events.clear();

    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T09:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    work.createEvent(new Event.Builder("C", s, e).build());

    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "events",
        "between", "2025-11-09",
        "and", "2025-11-11",
        "--target", "Personal",
        "to", "2025-11-25"
    ));
    cmd.execute(m);

    assertTrue(((FakeCalendar) personal).events.size() >= 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsOnInvalidSyntaxMissingOnOrTo() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "events",
        "2025-11-10",
        "--target", "Personal",
        "to", "2025-11-20"
    ));
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyEventsBetweenInvalidSyntaxMissingKeywords() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "events",
        "between", "2025-11-09",
        "--target", "Personal",
        "to", "2025-11-25"
    ));
    cmd.execute(m);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCopyInvalidTypeKeyword() {
    FakeManager m = new FakeManager();
    CopyCommand cmd = new CopyCommand(Arrays.asList(
        "copy", "weird",
        "on", "2025-11-10",
        "--target", "Work",
        "to", "2025-11-20"
    ));
    cmd.execute(m);
  }

  // ----------------------------------------------------------
  // EditEventCommand – all scopes + property coercion & errors
  // ----------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventManagerNull() {
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "\"Task\""), "event");
    cmd.execute(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testEditEventNoActiveCalendar() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "location", "Zoom"), "event");
    cmd.execute(new NoActiveManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventTooFewArgs() {
    EditEventCommand cmd = new EditEventCommand(
        Arrays.asList("edit", "event", "\"Task\"", "from"), "event");
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventMissingFromKeyword() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "with", "location", "Zoom"), "event");
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventMissingWithSection() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00"), "event");
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventUnknownProperty() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "nonsense", "value"), "event");
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidDateTimeInProperty() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "start", "not-a-date"), "event");
    cmd.execute(new FakeManager());
  }

  @Test
  public void testEditEventScopeEventStartEndSubjectDescriptionLocationStatus() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();

    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T09:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    cal.events.add(new Event.Builder("Task", s, e).build());

    // start
    EditEventCommand startCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "start", "2025-11-10T09:30"), "event");
    startCmd.execute(m);

    // end
    EditEventCommand endCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "end", "2025-11-10T10:30"), "event");
    endCmd.execute(m);

    // subject
    EditEventCommand subjCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "subject", "\"New Task\""), "event");
    subjCmd.execute(m);

    // description
    EditEventCommand descCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "description", "\"Desc\""), "event");
    descCmd.execute(m);

    // location
    EditEventCommand locCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "location", "\"Room\""), "event");
    locCmd.execute(m);

    // status
    EditEventCommand statusCmd = new EditEventCommand(Arrays.asList(
        "edit", "event", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "status", "PUBLIC"), "event");
    statusCmd.execute(m);

    // Just ensure model.editEvent was called multiple times
    assertTrue(cal.calls.stream().anyMatch(c -> c.startsWith("edit:")));
  }

  @Test
  public void testEditEventsScopeAndSeriesScope() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.events.clear();
    cal.calls.clear();

    ZonedDateTime s = ParseUtils.parseDateTimeEst("2025-11-10T09:00");
    ZonedDateTime e = ParseUtils.parseDateTimeEst("2025-11-10T10:00");
    cal.events.add(new Event.Builder("Standup", s, e).build());

    // events -> FROM_THIS_ONWARD
    EditEventCommand eventsCmd = new EditEventCommand(Arrays.asList(
        "edit", "events", "\"Standup\"",
        "from", "2025-11-10T09:00",
        "with", "location", "\"Zoom\""), "events");
    eventsCmd.execute(m);

    // series -> ENTIRE_SERIES
    EditEventCommand seriesCmd = new EditEventCommand(Arrays.asList(
        "edit", "series", "\"Standup\"",
        "from", "2025-11-10T09:00",
        "with", "status", "PRIVATE"), "series");
    seriesCmd.execute(m);

    assertTrue(cal.calls.stream()
        .anyMatch(c -> c.startsWith("editSeries:" + EditMode.FROM_THIS_ONWARD)));
    assertTrue(cal.calls.stream()
        .anyMatch(c -> c.startsWith("editSeries:" + EditMode.ENTIRE_SERIES)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditEventInvalidScope() {
    EditEventCommand cmd = new EditEventCommand(Arrays.asList(
        "edit", "weird", "\"Task\"",
        "from", "2025-11-10T09:00",
        "to", "2025-11-10T10:00",
        "with", "location", "Zoom"), "weird");
    cmd.execute(new FakeManager());
  }

  // ----------------------------------------------------------
  // ShowStatusCommand – busy/available + all error branches
  // ----------------------------------------------------------

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatusManagerNull() {
    ShowStatusCommand cmd = new ShowStatusCommand(Arrays.asList("on", "2025-11-10T10:00"));
    cmd.execute(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testShowStatusNoActiveCalendar() {
    ShowStatusCommand cmd = new ShowStatusCommand(Arrays.asList("on", "2025-11-10T10:00"));
    cmd.execute(new NoActiveManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatusTooFewArgs() {
    ShowStatusCommand cmd = new ShowStatusCommand(Collections.singletonList("on"));
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatusBadKeyword() {
    ShowStatusCommand cmd = new ShowStatusCommand(
        Arrays.asList("at", "2025-11-10T10:00"));
    cmd.execute(new FakeManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShowStatusInvalidDateFormat() {
    ShowStatusCommand cmd = new ShowStatusCommand(
        Arrays.asList("on", "not-a-date"));
    cmd.execute(new FakeManager());
  }

  @Test
  public void testShowStatusBusyAndAvailable() {
    FakeManager m = new FakeManager();
    FakeCalendar cal = (FakeCalendar) m.getActiveCalendar();
    cal.calls.clear();

    // isBusy implements hour%2==0 (even hour -> busy, odd -> available)
    ShowStatusCommand busyCmd = new ShowStatusCommand(
        Arrays.asList("on", "2025-11-10T10:00"));
    busyCmd.execute(m);

    ShowStatusCommand availCmd = new ShowStatusCommand(
        Arrays.asList("on", "2025-11-10T13:00"));
    availCmd.execute(m);

    // Just exercising both branches of model.isBusy()
    assertTrue(cal.calls.contains("busy"));
  }
}

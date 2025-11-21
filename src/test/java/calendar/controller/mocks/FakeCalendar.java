package calendar.controller.mocks;

import calendar.model.EditMode;
import calendar.model.Event;
import calendar.model.EventKey;
import calendar.model.Icalendar;
import calendar.model.RecurrenceRule;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal fake implementation of {@link Icalendar} for controller-layer testing.
 * Avoids file I/O and persists only in memory.
 */
public class FakeCalendar implements Icalendar {

  /** Records method calls for behavioral verification in tests. */
  public final List<String> calls = new ArrayList<>();

  /** Stores dummy in-memory events for copy/edit queries. */
  public final List<Event> events = new ArrayList<>();

  private ZoneId zone = ZoneId.of("America/New_York");

  /** Constructs a fake calendar and preloads one dummy event. */
  public FakeCalendar() {
    ZonedDateTime start =
        ZonedDateTime.of(2025, 11, 10, 9, 0, 0, 0, ZoneId.of("America/New_York"));
    ZonedDateTime end = start.plusHours(1);

    Event dummy = new Event.Builder("Meeting", start, end)
        .description("desc")
        .location("loc")
        .build();

    events.add(dummy);
  }

  @Override
  public ZoneId getZone() {
    return zone;
  }

  @Override
  public void setZone(ZoneId zone) {
    this.zone = zone; // simple assignment, no validation needed
  }

  @Override
  public List<Event> getAllEvents() {
    return events;
  }

  @Override
  public boolean isBusy(ZonedDateTime ts) {
    calls.add("busy");
    // deterministic pseudo busy logic
    return ts.getHour() % 2 == 0;
  }

  @Override
  public List<Event> queryEventsOn(LocalDate date) {
    calls.add("queryOn");
    return events;
  }

  @Override
  public List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    calls.add("queryBetween");
    return events;
  }

  @Override
  public void createEvent(Event e) {
    calls.add("create:" + e.getSubject());
    events.add(e);
  }

  @Override
  public void editEvent(EventKey key, String property, Object newValue) {
    calls.add("edit:" + property);
  }

  @Override
  public void editSeries(
      EventKey key, String property, Object newValue, EditMode mode) {
    calls.add("editSeries:" + mode);
  }

  @Override
  public void createSeries(Event e, RecurrenceRule rule) {
    calls.add("series");
  }
}

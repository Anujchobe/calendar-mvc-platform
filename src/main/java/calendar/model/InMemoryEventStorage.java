package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight in-memory storage for testing CalendarModel.
 * Stores events in a simple list and supports basic add/remove/query operations.
 */
public class InMemoryEventStorage implements IeventStorage {

  private final List<Event> events = new ArrayList<>();

  @Override
  public boolean addEvent(Event e) {
    for (Event existing : events) {
      if (existing.equals(e)) { // Duplicate check by subject, start, end
        return false;
      }
    }
    events.add(e);
    return true;
  }

  @Override
  public boolean removeEvent(EventKey key) {
    return events.removeIf(e -> e.matchesKey(key));
  }

  @Override
  public List<Event> getEventsOn(LocalDate date) {
    List<Event> result = new ArrayList<>();
    for (Event e : events) {
      if (e.occursOn(date)) {
        result.add(e);
      }
    }
    return result;
  }

  @Override
  public List<Event> getEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    List<Event> result = new ArrayList<>();
    for (Event e : events) {
      if (e.overlaps(start, end)) {
        result.add(e);
      }
    }
    return result;
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events); // Return a copy for safety
  }
}

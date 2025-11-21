package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * This class stores and manages calendar events using a TreeSet.
 *
 * <p>The TreeSet keeps events sorted and ensures uniqueness based on the
 * {@link Event#compareTo(Event)} implementation.
 * It supports adding, removing, and retrieving events efficiently.
 */
public class TreeSetEventStorage implements IeventStorage {

  /**
   * A sorted set that holds all events.
   */
  private final NavigableSet<Event> events;

  /**
   * Creates an empty TreeSetEventStorage instance.
   * The TreeSet automatically maintains event order.
   */
  public TreeSetEventStorage() {
    this.events = new TreeSet<>();
  }

  /**
   * Adds a new event to the storage.
   *
   * @param e the event to add
   * @return true if the event was added, false if it already exists
   */
  @Override
  public boolean addEvent(Event e) {
    return events.add(e);
  }

  /**
   * Removes an event that matches the given key.
   *
   * @param key the key representing the event to remove
   * @return true if an event was removed, false otherwise
   */
  @Override
  public boolean removeEvent(EventKey key) {
    return events.removeIf(ev -> ev.matchesKey(key));
  }

  /**
   * Gets all events that occur on the specified date.
   *
   * @param date the date to check
   * @return a list of events occurring on that date
   */
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

  /**
   * Gets all events that overlap with a given time range.
   *
   * @param start the start time
   * @param end   the end time
   * @return a list of events overlapping the time range
   */
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

  /**
   * Returns all stored events as a list.
   *
   * @return a list of all events in sorted order
   */
  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }
}

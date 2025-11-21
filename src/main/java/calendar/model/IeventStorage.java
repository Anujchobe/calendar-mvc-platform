package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Defines a contract for storing and retrieving calendar events.
 *
 *  <p>Implementations may use in-memory structures -Treeset or persistent storage.
 * Each event must be unique based on its subject, start, and end time.</p>
 */
public interface IeventStorage {

  /**
   * Adds a new event to the storage.
   *
   * @param e the event to add
   * @return {@code true} if added successfully; {@code false} if a duplicate exists
   */
  boolean addEvent(Event e);

  /**
   * Removes an event from storage using its unique key.
   *
   * @param key the unique identifier of the event
   * @return {@code true} if the event was removed; {@code false} if not found
   */
  boolean removeEvent(EventKey key);

  /**
   * Retrieves all events scheduled on a given date.
   *
   * @param date the date to query
   * @return a list of matching events
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Retrieves all events occurring between the given date-time range.
   *
   * @param start the start of the range
   * @param end   the end of the range
   * @return a list of events that overlap the range
   */
  List<Event> getEventsBetween(ZonedDateTime start, ZonedDateTime end);

  /**
   * Returns all stored events, typically in sorted order.
   *
   * @return a list of all stored events
   */
  List<Event> getAllEvents();
}

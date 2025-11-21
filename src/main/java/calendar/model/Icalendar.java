package calendar.model;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


/**
 * Represents a generic calendar capable of managing events and event series.
 *
 * <p>This interface defines the core business operations a calendar must support,
 * including creation, editing, querying, and exporting of events. Implementations
 * should ensure that no two events share the same subject, start, and end time.</p>
 *
 * <p>Initially, this supports a single calendar in a single timezone (EST).
 * However, the design allows extension to multiple calendars and timezones
 * without modification of existing code.</p>
 */
public interface Icalendar {

  /**
   * Creates a single event in the calendar.
   *
   * @param e the event to create (immutable)
   */
  void createEvent(Event e);

  /**
   * Creates a recurring series of events based on the given rule.
   *
   * @param e    the base (seed) event
   * @param rule the recurrence rule defining repetition pattern
   */
  void createSeries(Event e, RecurrenceRule rule);

  /**
   * Edits a single event identified by its unique key.
   *
   * @param key      identifies the event (subject, start, end)
   * @param property the property to edit (e.g. subject, time, status)
   * @param newValue the new value of the property
   */
  void editEvent(EventKey key, String property, Object newValue);

  /**
   * Edits an event series starting from a specified instance.
   *
   * @param key      identifies the event in the series
   * @param property the property to edit
   * @param newValue the new property value
   * @param mode     specifies which subset of events to edit (single, onward, entire)
   */
  void editSeries(EventKey key, String property, Object newValue, EditMode mode);

  /**
   * Retrieves all events scheduled on a given date.
   *
   * @param date the date to query
   * @return list of events occurring on that date
   */
  List<Event> queryEventsOn(LocalDate date);

  /**
   * Retrieves all events that fall within a given date-time range.
   *
   * @param start start time of the interval
   * @param end   end time of the interval
   * @return list of events within the interval
   */
  List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end);

  /**
   * Checks if the calendar is busy at a specific timestamp.
   *
   * @param timestamp date-time to check
   * @return true if an event overlaps the time, false otherwise
   */
  boolean isBusy(ZonedDateTime timestamp);

  /**
   * Retrieves an immutable list of all events currently stored in this calendar.
   *
   * <p>The returned list provides read-only access to the events to ensure that
   * external modifications do not affect the internal state of the calendar.
   *
   * @return an unmodifiable {@link List} containing all {@link Event} objects in the calendar
   */
  List<Event> getAllEvents();

  /**
   * Returns the timezone associated with this calendar.
   *
   * @return the calendar's timezone
   */
  ZoneId getZone();

  /**
   * Updates the timezone for this calendar.
   *
   * @param zone the new timezone to apply
   */
  void setZone(ZoneId zone);
}

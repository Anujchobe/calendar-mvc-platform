package calendar.model;

import java.time.ZoneId;
import java.util.List;

/**
 * Represents the manager responsible for handling multiple calendar instances.
 *
 * <p>This interface defines the operations for creating, editing,
 * switching, and retrieving calendars. Each calendar has a unique name
 * and an associated timezone. The manager also tracks the currently active
 * calendar that is in use by the application.</p>
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Manage a collection of calendars identified by unique names.</li>
 *   <li>Allow editing calendar properties such as name or timezone.</li>
 *   <li>Allow selecting an active calendar for operations.</li>
 * </ul>
 * </p>
 *
 * <p>Follows the MVC pattern by serving as the model component responsible
 * for calendar-level management (not event-level logic).</p>
 */
public interface IcalendarManager {

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar (must be unique and non-blank)
   * @param timezone the timezone for this calendar (cannot be null)
   * @throws IllegalArgumentException if the name already exists or is invalid
   */
  void createCalendar(String name, ZoneId timezone);

  /**
   * Edits an existing calendar's property, such as its name or timezone.
   *
   * @param name      the current name of the calendar
   * @param property  the property to edit ("name" or "timezone")
   * @param newValue  the new value to set for the property
   * @throws IllegalArgumentException if the calendar or property is invalid
   */
  void editCalendar(String name, String property, String newValue);

  /**
   * Sets the active calendar by its name.
   * Once a calendar is active, all event operations will target it.
   *
   * @param name the name of the calendar to activate
   * @throws IllegalArgumentException if the specified calendar does not exist
   */
  void useCalendar(String name);

  /**
   * Returns the currently active calendar in use.
   *
   * @return the active {@link Icalendar} instance
   * @throws IllegalStateException if no calendar is currently active
   */
  Icalendar getActiveCalendar();

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the calendar name
   * @return the {@link Icalendar} instance, or null if not found
   */
  Icalendar getCalendar(String name);

  /**
   * Returns a list of all calendar names managed by this instance.
   *
   * @return list of calendar names
   */
  List<String> listCalendars();
}

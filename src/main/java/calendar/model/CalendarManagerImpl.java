package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link IcalendarManager} interface that manages multiple calendars.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Creating and storing multiple calendars identified by unique names.</li>
 *   <li>Editing calendar-level properties such as name or timezone.</li>
 *   <li>Tracking and switching between active calendars.</li>
 * </ul>
 * </p>
 *
 */
public class CalendarManagerImpl implements IcalendarManager {

  /**
   * Maps calendar names to their corresponding {@link Icalendar} instances.
   * Each key (calendar name) must be unique.
   */
  private final Map<String, Icalendar> calendars;

  /**
   * The currently active calendar that is in use.
   */
  private Icalendar activeCalendar;

  /**
   * Constructs a new, empty calendar manager.
   * Initializes an empty calendar collection.
   */
  public CalendarManagerImpl() {
    this.calendars = new HashMap<>();
    this.activeCalendar = null;
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the name of the calendar (must be unique and non-blank)
   * @param timezone the timezone for this calendar (cannot be null)
   * @throws IllegalArgumentException if the name already exists or is invalid
   */
  @Override
  public void createCalendar(String name, ZoneId timezone) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Calendar name cannot be null or blank.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists.");
    }

    Icalendar newCalendar = new CalendarModel(new TreeSetEventStorage(), timezone);
    calendars.put(name, newCalendar);

    // If this is the first calendar created, make it active by default
    if (activeCalendar == null) {
      activeCalendar = newCalendar;
    }
  }

  /**
   * Edits an existing calendar's property, such as its name or timezone.
   *
   * @param name      the current name of the calendar
   * @param property  the property to edit ("name" or "timezone")
   * @param newValue  the new value to set for the property
   * @throws IllegalArgumentException if the calendar or property is invalid
   */
  @Override
  public void editCalendar(String name, String property, String newValue) {
    Icalendar cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("No calendar found with name: " + name);
    }

    switch (property.toLowerCase()) {
      case "name":
        if (newValue == null || newValue.isBlank()) {
          throw new IllegalArgumentException("New name cannot be null or blank.");
        }
        if (calendars.containsKey(newValue)) {
          throw new IllegalArgumentException("A calendar with that name already exists.");
        }

        calendars.put(newValue, cal);
        calendars.remove(name);
        break;

      case "timezone":
        if (newValue == null || newValue.isBlank()) {
          throw new IllegalArgumentException("Timezone cannot be null or blank.");
        }

        ZoneId newZone;
        try {
          newZone = ZoneId.of(newValue);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid timezone: " + newValue);
        }

        cal.setZone(newZone);
        break;

      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }

  /**
   * Sets the active calendar by its name.
   * Once a calendar is active, all event operations will target it.
   *
   * @param name the name of the calendar to activate
   * @throws IllegalArgumentException if the specified calendar does not exist
   */
  @Override
  public void useCalendar(String name) {
    Icalendar cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar '" + name + "' not found.");
    }
    this.activeCalendar = cal;
  }

  /**
   * Returns the currently active calendar in use.
   *
   * @return the active {@link Icalendar} instance
   * @throws IllegalStateException if no calendar is currently active
   */
  @Override
  public Icalendar getActiveCalendar() {
    if (activeCalendar == null) {
      throw new IllegalStateException("No active calendar selected.");
    }
    return activeCalendar;
  }

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the calendar name
   * @return the {@link Icalendar} instance, or null if not found
   */
  @Override
  public Icalendar getCalendar(String name) {
    return calendars.get(name);
  }

  /**
   * Returns a list of all calendar names managed by this instance.
   *
   * @return list of calendar names
   */
  @Override
  public List<String> listCalendars() {
    return new ArrayList<>(calendars.keySet());
  }
}

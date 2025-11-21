package calendar.controller;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * The {@code Ifeatures} interface defines all user-triggered actions
 * that the View can request from the Controller in the MVC architecture.
 *
 * <p>This is the ONLY entry point that the GUI uses to interact with
 * the system. The View never touches the Model directly, ensuring:</p>
 *
 * <ul>
 *   <li>Low coupling between MVC layers</li>
 *   <li>Testability</li>
 *   <li>SOLID (Controller obeys ISP & DIP)</li>
 *   <li>Flexibility during View–Controller parallel development</li>
 * </ul>
 *
 * <p>The controller implementing this interface must translate each method
 * call into appropriate model operations, validation, and error handling.</p>
 */
public interface Ifeatures {

  // ----------------------------------------------------------------------
  //  CALENDAR MANAGEMENT
  // ----------------------------------------------------------------------

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the unique name of the calendar to create
   * @param timezone a valid IANA timezone string (e.g., "America/New_York")
   */
  void createCalendar(String name, String timezone);

  /**
   * Switches the active calendar that all subsequent operations target.
   *
   * @param name the name of the calendar to activate
   *             (must correspond to an existing calendar)
   */
  void useCalendar(String name);

  /**
   * Returns a list of existing calendar names.
   *
   * <p>The GUI uses this to refresh dropdowns, selectors,
   * or color-coded calendar lists.</p>
   *
   * @return all calendar names currently managed by the system
   */
  List<String> getCalendarNames();

  // ----------------------------------------------------------------------
  //  EVENT CREATION
  // ----------------------------------------------------------------------

  /**
   * Creates a single, non-recurring event in the active calendar.
   *
   * @param subject     the event title
   * @param start       event start time (timezone must already be applied)
   * @param end         event end time (must be after start)
   * @param description optional event description (nullable)
   * @param location    optional location string (nullable)
   * @param status      event privacy level (PUBLIC or PRIVATE)
   */
  void createSingleEvent(
      String subject,
      ZonedDateTime start,
      ZonedDateTime end,
      String description,
      String location,
      EventStatus status
  );

  /**
   * Creates a recurring event following a weekly pattern (e.g., "MWF").
   *
   * <p>Exactly one of {@code occurrences} or {@code until} must be non-null.
   * The controller must reject cases where both or neither are provided.</p>
   *
   * @param subject        the event title
   * @param start          start of the first occurrence
   * @param end            end of the first occurrence
   * @param description    optional description (nullable)
   * @param location       optional location (nullable)
   * @param status         event privacy level (PUBLIC or PRIVATE)
   * @param weekdayPattern pattern string such as "MTRF", "MWF", or "SU"
   * @param occurrences    number of times to repeat, or {@code null}
   * @param until          date after which the event stops, or {@code null}
   */
  void createRecurringEvent(
      String subject,
      ZonedDateTime start,
      ZonedDateTime end,
      String description,
      String location,
      EventStatus status,
      String weekdayPattern,
      Integer occurrences,
      LocalDate until
  );

  /**
   * Performs an in-place update to a single event identified by
   * its subject and original start time.
   *
   * <p>The {@code property} string determines what field is updated.
   * Supported values (recommended): "subject", "start", "end",
   * "location", "description", "status".</p>
   *
   * @param subject  the original event subject
   * @param start    the original start datetime identifying the event
   * @param property which field to update
   * @param newValue the new value for the property
   */
  void editSingleEvent(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  );

  /**
   * Updates all future events in a recurring series from (and including)
   * the specified starting event.
   *
   * @param subject  event series subject
   * @param start    start time of the specific event that begins the update
   * @param property the field to update
   * @param newValue new value for that field
   */
  void editSeriesFromThisOnward(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  );

  /**
   * Updates every occurrence of a recurring event series.
   *
   * @param subject  title of the recurring event series
   * @param start    start time of one event in the series (to identify it)
   * @param property event field being modified
   * @param newValue replacement value
   */
  void editEntireSeries(
      String subject,
      ZonedDateTime start,
      String property,
      Object newValue
  );

  /**
   * Fetches all events that occur on a specific date in the
   * active calendar's timezone.
   *
   * @param date the date selected in the month grid
   * @return list of events scheduled for that day (empty list if none)
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Allows the user to navigate from one month to another in the UI.
   *
   * <p>Unlike offset-based navigation, the GUI passes the full
   * {@link YearMonth} the user wants to display.</p>
   *
   * @param month the calendar month to render in the UI
   */
  void navigateToMonth(YearMonth month);
}

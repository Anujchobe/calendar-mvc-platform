package calendar.controller;

import calendar.model.EditMode;
import calendar.model.Event;
import calendar.model.EventKey;
import calendar.model.EventStatus;
import calendar.model.RecurrenceRule;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Controller interface defining all features/actions that can be
 * triggered by the view.
 *
 * <p><b>Architecture: Controller as Listener</b></p>
 * <ul>
 *     <li>View fires events by calling methods in this interface</li>
 *     <li>Controller implements this interface and listens</li>
 *     <li>Controller delegates to model and updates view</li>
 * </ul>
 *
 *
 */
public interface Ifeatures {


  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * <p>Maps to: {@code manager.createCalendar(name, ZoneId.of(timezone))}</p>
   *
   * @param name     unique calendar identifier
   * @param timezone IANA timezone string (e.g., "America/New_York")
   * @throws IllegalArgumentException if inputs are invalid or calendar exists
   */
  void createCalendar(String name, String timezone);

  /**
   * Switches the active calendar context.
   *
   * <p>Maps to: {@code manager.useCalendar(calendarName)}</p>
   *
   * @param calendarName name of the calendar to use
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  void useCalendar(String calendarName);

  /**
   * Retrieves all available calendar names.
   *
   * <p>Maps to: {@code manager.listCalendars()}</p>
   *
   * @return list of calendar identifiers
   */
  List<String> getCalendarNames();

  /**
   * Gets the name of the currently active calendar.
   *
   * <p>Implementation can track this or derive from manager state.</p>
   *
   * @return current calendar name, or null if none active
   */
  String getCurrentCalendarName();


  /**
   * Creates a single (non-recurring) event.
   *
   * <p>Maps to: {@code calendar.createEvent(event)}</p>
   *
   * <p>Uses {@link calendar.model.Event.Builder} to construct event.</p>
   *
   * @param subject     event title
   * @param start       event start time with timezone
   * @param end         event end time with timezone
   * @param description event description (can be empty)
   * @param location    event location (can be empty)
   * @param status      PUBLIC or PRIVATE
   * @throws IllegalArgumentException if event data is invalid
   */
  void createSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                         String description, String location, EventStatus status);

  /**
   * Creates a recurring event that repeats on specific weekdays.
   *
   * <p>Maps to: {@code calendar.createSeries(event, rule)}</p>
   *
   * <p>Constructs {@link RecurrenceRule} from weekday pattern and termination.</p>
   *
   * @param subject        event title
   * @param start          first occurrence start time
   * @param end            first occurrence end time
   * @param description    event description
   * @param location       event location
   * @param status         PUBLIC or PRIVATE
   * @param weekdayPattern pattern string like "MTWRF" or "MWF"
   *                       (M=Mon, T=Tue, W=Wed, R=Thu, F=Fri, S=Sat, U=Sun)
   * @param occurrences    number of times to repeat (null if using endDate)
   * @param endDate        last date for recurrence (null if using occurrences)
   * @throws IllegalArgumentException if event data is invalid or both/neither
   *                                  occurrences and endDate are specified
   */
  void createRecurringEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                            String description, String location, EventStatus status,
                            String weekdayPattern, Integer occurrences, LocalDate endDate);


  /**
   * Updates a single event identified by its subject, start time, and end time.
   *
   * <p>Maps to: {@code calendar.editEvent(key, property, newValue)}</p>
   *
   * <p>Constructs {@link EventKey} using subject, start, and end times.</p>
   *
   * <p><b>Supported properties:</b></p>
   * <ul>
   *     <li>"subject" - String</li>
   *     <li>"start" - ZonedDateTime</li>
   *     <li>"end" - ZonedDateTime</li>
   *     <li>"location" - String</li>
   *     <li>"description" - String</li>
   *     <li>"status" - String (uppercase: "PUBLIC" or "PRIVATE")</li>
   * </ul>
   *
   * @param subject  the original event subject
   * @param start    the original start datetime identifying the event
   * @param end      the original end datetime identifying the event
   * @param property which field to update (see supported properties above)
   * @param newValue the new value for the property (type depends on property)
   * @throws IllegalArgumentException if event not found or property invalid
   */
  void editSingleEvent(String subject, ZonedDateTime start, ZonedDateTime end,
                       String property, Object newValue);

  /**
   * Updates all future events in a recurring series from (and including)
   * the specified starting event.
   *
   * <p>Maps to: {@code calendar.editSeries(key, property, newValue, EditMode.FROM_THIS_ONWARD)}</p>
   *
   * <p>This matches the "edit events" command with scope="events".</p>
   *
   * @param subject  event series subject
   * @param start    start time of the specific event that begins the update
   * @param end      end time of the specific event that begins the update
   * @param property the field to update
   * @param newValue new value for that field
   * @throws IllegalArgumentException if series not found or property invalid
   */
  void editSeriesFromThisOnward(String subject, ZonedDateTime start, ZonedDateTime end,
                                String property, Object newValue);

  /**
   * Updates every occurrence of a recurring event series.
   *
   * <p>Maps to: {@code calendar.editSeries(key, property, newValue, EditMode.ENTIRE_SERIES)}</p>
   *
   * <p>This matches the "edit series" command with scope="series".</p>
   *
   * @param subject  title of the recurring event series
   * @param start    start time of one event in the series (to identify it)
   * @param end      end time of one event in the series (to identify it)
   * @param property event field being modified
   * @param newValue replacement value
   * @throws IllegalArgumentException if series not found or property invalid
   */
  void editEntireSeries(String subject, ZonedDateTime start, ZonedDateTime end,
                        String property, Object newValue);


  /**
   * Fetches all events that occur on a specific date in the
   * active calendar's timezone.
   *
   * <p>Maps to: {@code calendar.queryEventsOn(date)}</p>
   *
   * @param date the date selected in the month grid
   * @return list of events scheduled for that day (empty list if none)
   */
  List<Event> getEventsOn(LocalDate date);

  /**
   * Allows the user to navigate from one month to another in the UI.
   *
   * <p>This is primarily a UI operation. Controller may use this to
   * pre-fetch events or track navigation state.</p>
   *
   * @param month the calendar month to render in the UI
   */
  void navigateToMonth(YearMonth month);

  /**
   * Called when the user clicks a specific date cell in the month view.
   *
   * <p>This informs the controller which date is currently selected,
   * allowing it to update event lists, perform validation, or adjust
   * navigation state.</p>
   *
   * @param date the specific date chosen by the user
   */
  void selectDate(LocalDate date);

}
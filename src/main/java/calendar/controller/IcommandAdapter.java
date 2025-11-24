package calendar.controller;

import calendar.model.EventStatus;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Interface responsible for building CLI-format command strings from GUI parameters.
 * Architecture:
 * View fires event with parameters
 *   ↓
 * GuiFeaturesController receives via Ifeatures methods
 *   ↓
 * Calls IcommandAdapter to build command string
 *   ↓
 * Returns command string (e.g., "create event 'Meeting' from ...")
 *   ↓
 * Pass to CommandParser/CommandFactory (existing CLI infrastructure)
 * This interface acts as the bridge between GUI parameters and CLI command format.
 */
public interface IcommandAdapter {

  /**
   * Builds a command string to create a calendar.
   *
   * @param name calendar name
   * @param timezone IANA timezone string
   * @return CLI command string: "create calendar --name  --timezone "
   */
  String buildCreateCalendarCommand(String name, String timezone);

  /**
   * Builds a command string to switch active calendar.
   *
   * @param calendarName name of calendar to use
   * @return CLI command string: "use calendar --name"
   */
  String buildUseCalendarCommand(String calendarName);

  /**
   * Builds a command string to create a single event.
   *
   * @param subject event title
   * @param start event start time
   * @param end event end time
   * @param description event description (can be null/empty)
   * @param location event location (can be null/empty)
   * @param status event status (PUBLIC/PRIVATE)
   * @return CLI command string: "create event 'Subject' from start to end ..."
   */
  String buildCreateSingleEventCommand(String subject, ZonedDateTime start,
                                       ZonedDateTime end, String description,
                                       String location, EventStatus status);

  /**
   * Builds a command string to create a recurring event.
   *
   * @param subject event title
   * @param start first occurrence start time
   * @param end first occurrence end time
   * @param description event description
   * @param location event location
   * @param status event status
   * @param weekdayPattern weekday pattern (e.g., "MTWRF")
   * @param occurrences number of occurrences (null if using endDate)
   * @param endDate end date for recurrence (null if using occurrences)
   * @return CLI command string: "create event 'Subject' from start to end repeats ..."
   */
  String buildCreateRecurringEventCommand(String subject, ZonedDateTime start,
                                          ZonedDateTime end, String description,
                                          String location, EventStatus status,
                                          String weekdayPattern, Integer occurrences,
                                          LocalDate endDate);

  /**
   * Builds a command string to edit a single event.
   *
   * @param subject event subject
   * @param start event start time
   * @param end event end time
   * @param property property to edit (e.g., "location", "description")
   * @param newValue new value for the property
   * @return CLI command string: "edit event 'Subject' from start to end with prop val"
   */
  String buildEditSingleEventCommand(String subject, ZonedDateTime start,
                                     ZonedDateTime end, String property, Object newValue);

  /**
   * Builds a command string to edit a series from a specific event onward.
   *
   * @param subject event subject
   * @param start start time of the event where editing begins
   * @param end end time of the event
   * @param property property to edit
   * @param newValue new value
   * @return CLI command string: "edit events 'Subject' from start with prop val"
   */
  String buildEditSeriesFromThisOnwardCommand(String subject, ZonedDateTime start,
                                              ZonedDateTime end, String property,
                                              Object newValue);

  /**
   * Builds a command string to edit an entire recurring series.
   *
   * @param subject event subject
   * @param start start time of any event in the series
   * @param end end time of the event
   * @param property property to edit
   * @param newValue new value
   * @return CLI command string: "edit series 'Subject' from start with prop val"
   */
  String buildEditEntireSeriesCommand(String subject, ZonedDateTime start,
                                      ZonedDateTime end, String property, Object newValue);
}
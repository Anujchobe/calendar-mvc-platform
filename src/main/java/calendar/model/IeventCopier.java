package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Interface for copying events within and across calendars.
 *
 * <p>This interface is segregated from {@link Icalendar} to follow the
 * Interface Segregation Principle (ISP). Classes that only need basic calendar
 * operations don't need to implement copy functionality.</p>
 *
 */
public interface IeventCopier {

  /**
   * Copies a specific event to a target calendar at a specified date/time.
   *
   * @param eventName   the subject of the event to copy
   * @param sourceStart the start date/time of the event in source calendar
   * @param targetCal   the destination calendar
   * @param targetStart the desired start date/time in target calendar's timezone
   * @throws IllegalArgumentException if event not found or target calendar is null
   */
  void copyEvent(String eventName, ZonedDateTime sourceStart,
                 Icalendar targetCal, ZonedDateTime targetStart);

  /**
   * Copies all events scheduled on a specific date to a target calendar.
   *
   * @param sourceDate the date in the source calendar
   * @param targetCal  the destination calendar
   * @param targetDate the corresponding date in the target calendar
   * @throws IllegalArgumentException if target calendar is null
   */
  void copyEventsOnDate(LocalDate sourceDate, Icalendar targetCal, LocalDate targetDate);

  /**
   * Copies all events within a date interval to a target calendar.
   * Events that partially overlap are included. For recurring events,
   * only instances within the range are copied, maintaining series status.
   *
   * @param startDate       start of the interval (inclusive)
   * @param endDate         end of the interval (inclusive)
   * @param targetCal       destination calendar
   * @param targetStartDate the date in target calendar corresponding to startDate
   * @throws IllegalArgumentException if dates are invalid or target calendar is null
   */
  void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                         Icalendar targetCal, LocalDate targetStartDate);
}
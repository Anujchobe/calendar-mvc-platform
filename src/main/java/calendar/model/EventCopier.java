package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of event copying operations.
 *
 * <p>Handles copying events within and across calendars with automatic timezone conversion,
 * property preservation, and recurring series management. Follows Single Responsibility
 * Principle by focusing solely on copy logic.</p>
 *
 * <p>Create instances using {@link EventCopierFactory} rather than direct instantiation.</p>
 *
 * @see IeventCopier
 * @see EventCopierFactory
 */
public class EventCopier implements IeventCopier {

  private final Icalendar sourceCalendar;

  /**
   * Constructs an EventCopier for a specific source calendar.
   *
   * @param sourceCalendar the calendar to copy events from
   * @throws IllegalArgumentException if sourceCalendar is null
   */
  public EventCopier(Icalendar sourceCalendar) {
    if (sourceCalendar == null) {
      throw new IllegalArgumentException("Source calendar cannot be null.");
    }
    this.sourceCalendar = sourceCalendar;
  }

  @Override
  public void copyEvent(String eventName, ZonedDateTime sourceStart,
                        Icalendar targetCal, ZonedDateTime targetStart) {
    validateInputs(eventName, targetCal);

    Event sourceEvent = findEventByNameAndStart(eventName, sourceStart);
    if (sourceEvent == null) {
      throw new IllegalArgumentException(
          "Event '" + eventName + "' not found at " + sourceStart);
    }

    Duration offset = Duration.between(sourceEvent.getStart(), targetStart);
    Event newEvent = createCopiedEvent(sourceEvent, offset, targetCal.getZone());
    targetCal.createEvent(newEvent);
  }

  @Override
  public void copyEventsOnDate(LocalDate sourceDate, Icalendar targetCal,
                               LocalDate targetDate) {
    validateDates(sourceDate, targetDate, targetCal);

    List<Event> eventsOnDate = sourceCalendar.queryEventsOn(sourceDate);
    long dayOffset = ChronoUnit.DAYS.between(sourceDate, targetDate);

    for (Event event : eventsOnDate) {
      try {
        copySingleEventWithOffset(event, targetCal, dayOffset);
      } catch (Exception e) {
        System.err.println("Warning: Failed to copy event '" + event.getSubject()
            + "': " + e.getMessage());
      }
    }
  }

  @Override
  public void copyEventsBetween(LocalDate startDate, LocalDate endDate,
                                Icalendar targetCal, LocalDate targetStartDate) {
    validateDateRange(startDate, endDate, targetStartDate, targetCal);

    ZonedDateTime rangeStart = startDate.atStartOfDay(sourceCalendar.getZone());
    ZonedDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(sourceCalendar.getZone());
    List<Event> eventsInRange = sourceCalendar.queryEventsBetween(rangeStart, rangeEnd);

    long dayOffset = ChronoUnit.DAYS.between(startDate, targetStartDate);
    EventCollection eventCollection = categorizeEvents(eventsInRange, startDate, endDate);

    copyStandaloneEvents(eventCollection.getStandaloneEvents(), targetCal, dayOffset);
    copyRecurringSeries(eventCollection.getSeriesMap(), targetCal, dayOffset,
        startDate, endDate);
  }

  /**
   * Validates common input parameters for copy operations.
   *
   * @param eventName the event name to validate
   * @param targetCal the target calendar to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateInputs(String eventName, Icalendar targetCal) {
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar cannot be null.");
    }
    if (eventName == null || eventName.isBlank()) {
      throw new IllegalArgumentException("Event name cannot be null or blank.");
    }
  }

  /**
   * Validates dates for single-date copy operation.
   *
   * @param sourceDate the source date to validate
   * @param targetDate the target date to validate
   * @param targetCal  the target calendar to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateDates(LocalDate sourceDate, LocalDate targetDate,
                             Icalendar targetCal) {
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar cannot be null.");
    }
    if (sourceDate == null || targetDate == null) {
      throw new IllegalArgumentException("Dates cannot be null.");
    }
  }

  /**
   * Validates date range for interval copy operation.
   *
   * @param startDate       the start date to validate
   * @param endDate         the end date to validate
   * @param targetStartDate the target start date to validate
   * @param targetCal       the target calendar to validate
   * @throws IllegalArgumentException if validation fails
   */
  private void validateDateRange(LocalDate startDate, LocalDate endDate,
                                 LocalDate targetStartDate, Icalendar targetCal) {
    if (targetCal == null) {
      throw new IllegalArgumentException("Target calendar cannot be null.");
    }
    if (startDate == null || endDate == null || targetStartDate == null) {
      throw new IllegalArgumentException("Dates cannot be null.");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date must be after or equal to start date.");
    }
  }

  /**
   * Finds an event by subject and start time in the source calendar.
   *
   * @param subject the subject to search for (case-insensitive)
   * @param start   the start time to match
   * @return the matching event, or null if not found
   */
  private Event findEventByNameAndStart(String subject, ZonedDateTime start) {
    for (Event e : sourceCalendar.getAllEvents()) {
      if (e.getSubject().equalsIgnoreCase(subject) && e.getStart().equals(start)) {
        return e;
      }
    }
    return null;
  }

  /**
   * Creates a copy of an event with time offset and timezone conversion.
   *
   * <p>Preserves all event properties including description, location, status,
   * and series ID. Applies timezone conversion to maintain proper local times.</p>
   *
   * @param source     the source event to copy
   * @param offset     the time offset to apply
   * @param targetZone the timezone of the target calendar
   * @return a new event with adjusted times and timezone
   */
  private Event createCopiedEvent(Event source, Duration offset, ZoneId targetZone) {
    ZonedDateTime newStart = source.getStart()
        .plus(offset)
        .withZoneSameInstant(targetZone);

    ZonedDateTime newEnd = source.getEnd()
        .plus(offset)
        .withZoneSameInstant(targetZone);

    return new Event.Builder(source.getSubject(), newStart, newEnd)
        .description(source.getDescription())
        .location(source.getLocation())
        .status(source.getStatus())
        .allDay(source.isAllDay())
        .seriesId(source.getSeriesId())
        .build();
  }

  /**
   * Copies a single event with day offset and timezone conversion.
   *
   * @param event     the event to copy
   * @param targetCal the target calendar
   * @param dayOffset the number of days to offset
   */
  private void copySingleEventWithOffset(Event event, Icalendar targetCal, long dayOffset) {
    ZonedDateTime newStart = event.getStart()
        .plusDays(dayOffset)
        .withZoneSameInstant(targetCal.getZone());

    Duration duration = Duration.between(event.getStart(), event.getEnd());
    ZonedDateTime newEnd = newStart.plus(duration);

    Event newEvent = new Event.Builder(event.getSubject(), newStart, newEnd)
        .description(event.getDescription())
        .location(event.getLocation())
        .status(event.getStatus())
        .allDay(event.isAllDay())
        .seriesId(event.getSeriesId())
        .build();

    targetCal.createEvent(newEvent);
  }

  /**
   * Categorizes events into standalone and recurring series for batch processing.
   *
   * @param events     the list of events to categorize
   * @param rangeStart the start date of the range (for filtering)
   * @param rangeEnd   the end date of the range (for filtering)
   * @return an EventCollection with separated standalone and series events
   */
  private EventCollection categorizeEvents(List<Event> events,
                                           LocalDate rangeStart,
                                           LocalDate rangeEnd) {
    Map<String, List<Event>> seriesMap = new HashMap<>();
    List<Event> standaloneEvents = new ArrayList<>();

    for (Event event : events) {
      LocalDate eventDate = event.getStart().toLocalDate();

      if (eventDate.isBefore(rangeStart) || eventDate.isAfter(rangeEnd)) {
        continue;
      }

      if (event.getSeriesId() != null) {
        seriesMap.computeIfAbsent(event.getSeriesId(), k -> new ArrayList<>())
            .add(event);
      } else {
        standaloneEvents.add(event);
      }
    }

    return new EventCollection(standaloneEvents, seriesMap);
  }

  /**
   * Copies all standalone events to the target calendar.
   *
   * @param standaloneEvents the list of standalone events to copy
   * @param targetCal        the target calendar
   * @param dayOffset        the number of days to offset
   */
  private void copyStandaloneEvents(List<Event> standaloneEvents,
                                    Icalendar targetCal,
                                    long dayOffset) {
    for (Event event : standaloneEvents) {
      try {
        ZonedDateTime newStart = event.getStart()
            .plusDays(dayOffset)
            .withZoneSameInstant(targetCal.getZone());

        Duration duration = Duration.between(event.getStart(), event.getEnd());
        ZonedDateTime newEnd = newStart.plus(duration);

        Event newEvent = new Event.Builder(event.getSubject(), newStart, newEnd)
            .description(event.getDescription())
            .location(event.getLocation())
            .status(event.getStatus())
            .allDay(event.isAllDay())
            .build();

        targetCal.createEvent(newEvent);
      } catch (Exception e) {
        System.err.println("Warning: Failed to copy event '" + event.getSubject()
            + "': " + e.getMessage());
      }
    }
  }

  /**
   * Copies recurring event series with a new series ID for the target calendar.
   *
   * <p>Preserves the series relationship but generates a new series ID to ensure
   * independence between source and target calendars.</p>
   *
   * @param seriesMap  map of series IDs to their events
   * @param targetCal  the target calendar
   * @param dayOffset  the number of days to offset
   * @param rangeStart the start date of the copy range
   * @param rangeEnd   the end date of the copy range
   */
  private void copyRecurringSeries(Map<String, List<Event>> seriesMap,
                                   Icalendar targetCal,
                                   long dayOffset,
                                   LocalDate rangeStart,
                                   LocalDate rangeEnd) {
    for (Map.Entry<String, List<Event>> entry : seriesMap.entrySet()) {
      List<Event> seriesEvents = entry.getValue();

      if (seriesEvents.isEmpty()) {
        continue;
      }

      seriesEvents.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
      String newSeriesId = UUID.randomUUID().toString();

      for (Event event : seriesEvents) {
        try {
          ZonedDateTime newStart = event.getStart()
              .plusDays(dayOffset)
              .withZoneSameInstant(targetCal.getZone());

          Duration duration = Duration.between(event.getStart(), event.getEnd());
          ZonedDateTime newEnd = newStart.plus(duration);

          Event newEvent = new Event.Builder(event.getSubject(), newStart, newEnd)
              .description(event.getDescription())
              .location(event.getLocation())
              .status(event.getStatus())
              .allDay(event.isAllDay())
              .seriesId(newSeriesId)
              .build();

          targetCal.createEvent(newEvent);
        } catch (Exception e) {
          System.err.println("Warning: Failed to copy event '" + event.getSubject()
              + "' from series: " + e.getMessage());
        }
      }
    }
  }
}
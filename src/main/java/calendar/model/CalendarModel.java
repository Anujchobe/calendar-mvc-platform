package calendar.model;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements the core model logic for the Virtual Calendar system.
 * Handles event creation, editing, querying, and exporting.
 * Uses injected interfaces for storage and export to ensure modularity.
 */
public class CalendarModel implements Icalendar {

  private final IeventStorage storage;
  private ZoneId zone;

  /**
   * Constructs a calendar model.
   *
   * @param storage event storage backend
   * @param zone    timezone (defaults to EST if null)
   */
  public CalendarModel(IeventStorage storage, ZoneId zone) {
    if (storage == null) {
      throw new IllegalArgumentException("Storage cannot be null.");
    }
    this.storage = storage;
    this.zone = (zone != null) ? zone : ZoneId.of("America/New_York");
  }

  /**
   * Adds a single event to the calendar.
   *
   * @param e event to add
   * @throws IllegalArgumentException if a duplicate event exists
   */
  @Override
  public void createEvent(Event e) {
    if (!storage.addEvent(e)) {
      throw new IllegalArgumentException("Duplicate or conflicting event: " + e.getSubject());
    }
  }

  /**
   * Adds a recurring series of events based on a recurrence rule.
   *
   * @param e    base event
   * @param rule recurrence rule for repetition
   * @throws IllegalArgumentException if duplicates occur in the series
   */
  @Override
  public void createSeries(Event e, RecurrenceRule rule) {
    List<? extends Event> series = rule.generateSeries(e);
    for (Event ev : series) {
      if (!storage.addEvent(ev)) {
        throw new IllegalArgumentException(
            "Duplicate found in recurring series: " + ev.getSubject());
      }
    }
  }

  /**
   * Edits a specific event identified by its key.
   *
   * @param key      event key
   * @param property property to edit
   * @param newValue new value to apply
   * @throws IllegalArgumentException if event not found
   */
  @Override
  public void editEvent(EventKey key, String property, Object newValue) {
    List<? extends Event> events = storage.getAllEvents();
    for (Event e : events) {
      if (e.matchesKey(key)) {
        Event modified = e.copyWith(property, newValue);
        storage.removeEvent(key);
        storage.addEvent(modified);
        return;
      }
    }
    throw new IllegalArgumentException("Event not found for editing.");
  }

  /**
   * Edits a recurring series based on the specified mode.
   *
   * @param key      reference event key
   * @param property property to edit
   * @param newValue new value
   * @param mode     edit mode (single, onward, entire series)
   */
  @Override
  public void editSeries(EventKey key, String property, Object newValue, EditMode mode) {
    List<Event> allEvents = new ArrayList<>(storage.getAllEvents());
    Event anchor = findAnchorEvent(allEvents, key);

    if (anchor == null) {
      throw new IllegalArgumentException("No matching event found for the given key.");
    }

    EventKey anchorKey = anchor.getKey();
    String seriesId = anchor.getSeriesId();


    switch (mode) {
      case SINGLE:
        updateSingleEvent(anchor, property, newValue);
        break;

      case FROM_THIS_ONWARD:
        updateSeriesFromThisOnward(allEvents, anchor, seriesId, anchorKey, property, newValue);
        break;

      case ENTIRE_SERIES:
        updateEntireSeries(allEvents, anchor, seriesId, property, newValue);
        break;

      default:
        throw new IllegalArgumentException("Unknown edit mode: " + mode);
    }
  }

  /**
   * Finds the reference (anchor) event corresponding to the provided key.
   */
  private Event findAnchorEvent(List<Event> events, EventKey key) {
    for (Event e : events) {
      boolean subjectMatches = e.getSubject().equalsIgnoreCase(key.getSubject());
      boolean startMatches = e.getStart().isEqual(key.getStart());
      boolean endMatches = key.getEnd() == null || e.getEnd().isEqual(key.getEnd());

      if (subjectMatches && startMatches && endMatches) {
        return e;
      }
    }
    return null;
  }


  /**
   * Updates only a single event instance.
   */
  private void updateSingleEvent(Event event, String property, Object newValue) {
    storage.removeEvent(event.getKey());
    storage.addEvent(event.copyWith(property, newValue));
  }

  /**
   * Updates all events from (and including) the anchor event onward in the same series.
   *
   * <p>Only events that belong to the same seriesId and have a start time
   * greater than or equal to the anchor event's start time will be updated.</p>
   *
   * @param events   all events currently in storage
   * @param anchor   the reference event (starting point)
   * @param seriesId ID of the series to update
   * @param key      the reference event key, used for comparison
   * @param property the property to update (e.g., "location", "description")
   * @param newValue the new value to apply to that property
   */
  private void updateSeriesFromThisOnward(List<Event> events, Event anchor, String seriesId,
                                          EventKey key, String property, Object newValue) {
    if (seriesId == null || seriesId.isBlank()) {
      updateSingleEvent(anchor, property, newValue);
      return;
    }

    boolean shouldSplitSeries =
        property.equalsIgnoreCase("start") || property.equalsIgnoreCase("end");
    String targetSeriesId = shouldSplitSeries ? UUID.randomUUID().toString() : seriesId;

    List<Event> toUpdate = collectFutureSeriesEvents(events, seriesId, key);

    // Compute proportional offsets for time-based edits
    long startOffsetHours = computeOffsetHours(property, anchor.getStart(), newValue, "start");
    long endOffsetHours = computeOffsetHours(property, anchor.getEnd(), newValue, "end");

    applyUpdatesToSeriesEvents(toUpdate, property, newValue, shouldSplitSeries,
        seriesId, targetSeriesId, startOffsetHours, endOffsetHours);
  }

  /**
   * Applies the computed change to each affected event and re-adds them
   * to storage. Handles both regular and split-series updates.
   */
  private void applyUpdatesToSeriesEvents(List<Event> toUpdate, String property, Object newValue,
                                          boolean shouldSplitSeries, String seriesId,
                                          String targetSeriesId,
                                          long startOffsetHours, long endOffsetHours) {
    for (Event e : toUpdate) {
      storage.removeEvent(e.getKey());

      // Adjust value proportionally for time changes
      Object adjustedValue = newValue;
      if (property.equalsIgnoreCase("start") && newValue instanceof ZonedDateTime) {
        adjustedValue = e.getStart().plusHours(startOffsetHours);
      } else if (property.equalsIgnoreCase("end") && newValue instanceof ZonedDateTime) {
        adjustedValue = e.getEnd().plusHours(endOffsetHours);
      }

      Event updated = e.copyWith(property, adjustedValue);

      // If series is split, give updated events a new seriesId
      if (shouldSplitSeries && !targetSeriesId.equals(seriesId)) {
        updated = new Event.Builder(updated.getSubject(), updated.getStart(), updated.getEnd())
            .description(updated.getDescription())
            .location(updated.getLocation())
            .status(updated.getStatus())
            .allDay(updated.isAllDay())
            .seriesId(targetSeriesId)
            .build();
      }

      storage.addEvent(updated);
    }
  }


  /**
   * Collects all events that belong to the same series and occur
   * on or after the specified key's start time.
   */
  private List<Event> collectFutureSeriesEvents(List<Event> events, String seriesId, EventKey key) {
    List<Event> toUpdate = new ArrayList<>();
    for (Event e : events) {
      boolean sameSeries = seriesId.equals(e.getSeriesId());
      boolean isCurrentOrFuture = !e.getStart().isBefore(key.getStart());
      if (sameSeries && isCurrentOrFuture) {
        toUpdate.add(e);
      }
    }
    return toUpdate;
  }

  /**
   * Computes the number of hours by which the start or end time changed
   * between the anchor and the new value. Returns 0 for non-time edits.
   */
  private long computeOffsetHours(String property, ZonedDateTime original,
                                  Object newValue, String expectedProperty) {
    if (property.equalsIgnoreCase(expectedProperty) && newValue instanceof ZonedDateTime) {
      return java.time.Duration.between(original, (ZonedDateTime) newValue).toHours();
    }
    return 0;
  }


  /**
   * Updates every event in the same series.
   */
  private void updateEntireSeries(List<Event> events, Event anchor, String seriesId,
                                  String property, Object newValue) {
    for (Event e : events) {
      boolean sameSeries =
          (seriesId != null && seriesId.equals(e.getSeriesId()))
              ||
              (seriesId == null && e.getSeriesId() == null
                  &&
                  e.getSubject().equals(anchor.getSubject()));

      if (sameSeries) {
        storage.removeEvent(e.getKey());
        storage.addEvent(e.copyWith(property, newValue));
      }
    }
  }


  /**
   * Retrieves all events occurring on a specific date.
   *
   * @param date target date
   * @return list of matching events
   */
  @Override
  public List<Event> queryEventsOn(LocalDate date) {
    return storage.getEventsOn(date);
  }

  /**
   * Retrieves events within a given time range.
   *
   * @param start start time
   * @param end   end time
   * @return list of matching events
   */
  @Override
  public List<Event> queryEventsBetween(ZonedDateTime start, ZonedDateTime end) {
    return storage.getEventsBetween(start, end);
  }

  /**
   * Checks if the user is busy at a specific timestamp.
   *
   * @param timestamp time to check
   * @return true if an event overlaps, false otherwise
   */
  @Override
  public boolean isBusy(ZonedDateTime timestamp) {
    for (Event e : storage.getAllEvents()) {
      if (!timestamp.isBefore(e.getStart()) && !timestamp.isAfter(e.getEnd())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns all events in this calendar.
   * Used by the controller for export operations.
   */
  @Override
  public List<Event> getAllEvents() {
    return storage.getAllEvents();
  }

  /**
   * Returns the timezone of this calendar.
   */
  @Override
  public ZoneId getZone() {
    return this.zone;
  }

  /**
   * Updates the calendar’s timezone.
   */
  @Override
  public void setZone(ZoneId newZone) {
    if (newZone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.zone = newZone;
  }

}

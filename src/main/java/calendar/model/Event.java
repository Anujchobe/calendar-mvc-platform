package calendar.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents an immutable calendar event with details such as subject, start and end times,
 * description, location, status, and series association.
 *
 * <p>This implementation uses the Builder pattern to simplify object creation
 * and ensure immutability. Any updates return a new {@code Event} instance.</p>
 */
public final class Event implements Comparable<Event> {

  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final String description;
  private final String location;
  private final EventStatus status;
  private final boolean allDay;
  private final String seriesId;

  /**
   * Private constructor for {@link Event}, invoked internally by {@link Builder}.
   *
   * @param builder the builder instance containing field values
   */
  private Event(Builder builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status == null ? EventStatus.PRIVATE : builder.status;
    this.allDay = builder.allDay;
    this.seriesId = (builder.seriesId != null && !builder.seriesId.isBlank())
        ? builder.seriesId : null;
  }


  /**
   * Builder for constructing immutable {@link Event} instances.
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * Event meeting = new Event.Builder("Team Sync", start, end)
   *     .description("Sprint review")
   *     .location("Conference Room A")
   *     .status(EventStatus.PUBLIC)
   *     .build();
   * }</pre>
   */
  public static class Builder {
    private final String subject;
    private final ZonedDateTime start;
    private final ZonedDateTime end;

    private String description;
    private String location;
    private EventStatus status;
    private boolean allDay;
    private String seriesId;

    /**
     * Constructs a builder with the required fields.
     *
     * @param subject event subject (non-null and non-blank)
     * @param start   start date-time (non-null)
     * @param end     end date-time (non-null and after start)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Builder(String subject, ZonedDateTime start, ZonedDateTime end) {
      if (subject == null || subject.isBlank()) {
        throw new IllegalArgumentException("Event subject cannot be null or blank");
      }
      if (start == null) {
        throw new IllegalArgumentException("Event start time cannot be null");
      }
      if (end == null) {
        throw new IllegalArgumentException("Event end time cannot be null");
      }
      if (!end.isAfter(start)) {
        throw new IllegalArgumentException("Event end time must be after start time");
      }

      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    /**
     * Sets the event description.
     *
     * @param description event description
     * @return this builder instance
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location event location
     * @return this builder instance
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event status.
     *
     * @param status visibility of the event (PUBLIC or PRIVATE)
     * @return this builder instance
     */
    public Builder status(EventStatus status) {
      this.status = status;
      return this;
    }

    /**
     * Marks the event as an all-day event (8 AM to 5 PM by default).
     *
     * @param allDay whether the event spans the full day
     * @return this builder instance
     */
    public Builder allDay(boolean allDay) {
      this.allDay = allDay;
      return this;
    }

    /**
     * Sets the series ID for recurring events.
     *
     * @param seriesId identifier for the series
     * @return this builder instance
     */
    public Builder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds and returns an immutable {@link Event}.
     *
     * @return a fully constructed Event
     */
    public Event build() {
      ZonedDateTime actualStart = this.start;
      ZonedDateTime actualEnd = this.end;

      if (this.allDay) {
        actualStart = start.withHour(8).withMinute(0);
        actualEnd = start.withHour(17).withMinute(0);
      }

      return new Event.Builder(this.subject, actualStart, actualEnd)
          .description(this.description)
          .location(this.location)
          .status(this.status)
          .seriesId(this.seriesId)
          .allDay(this.allDay)
          .finalBuild();
    }

    /**
     * Internal helper to create the final immutable Event instance.
     *
     * @return the constructed Event
     */
    private Event finalBuild() {
      return new Event(this);
    }
  }

  /**
   * Returns this event's identifying key.
   *
   * @return a unique {@link EventKey} for this event
   */
  public EventKey getKey() {
    return new EventKey(subject, start, end);
  }


  /**
   * Returns the subject or title of this event.
   *
   * @return the subject of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the starting date and time of this event.
   *
   * @return the event start date-time
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Returns the ending date and time of this event.
   *
   * @return the event end date-time
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Returns the description or notes associated with this event.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the physical or online location of this event.
   *
   * @return the event location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Returns the visibility status of this event (PUBLIC or PRIVATE).
   *
   * @return the event visibility status
   */
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Indicates whether this event spans the entire day.
   *
   * @return true if the event is all-day, false otherwise
   */
  public boolean isAllDay() {
    return allDay;
  }

  /**
   * Returns the recurring series ID if this event is part of one.
   *
   * @return the series ID, or null if this is a standalone event
   */
  public String getSeriesId() {
    return seriesId;
  }


  /**
   * Checks if the event occurs on a given date.
   *
   * @param date date to check
   * @return true if event overlaps the given date
   */
  public boolean occursOn(LocalDate date) {
    return !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate());
  }

  /**
   * Checks if this event overlaps with the given time range.
   *
   * @param startRange start of the range
   * @param endRange   end of the range
   * @return true if this event overlaps with the range
   */
  public boolean overlaps(ZonedDateTime startRange, ZonedDateTime endRange) {
    return !(end.isBefore(startRange) || start.isAfter(endRange));
  }

  /**
   * Creates a new event with one property updated, preserving immutability.
   *
   * @param property name of the property to modify
   * @param newValue new value for the property
   * @return a modified copy of the event
   */
  public Event copyWith(String property, Object newValue) {
    Builder builder = new Builder(subject, start, end)
        .description(description)
        .location(location)
        .status(status)
        .seriesId(seriesId)
        .allDay(allDay);

    switch (property.toLowerCase()) {
      case "subject":
        builder = new Builder((String) newValue, start, end)
            .description(description).location(location)
            .status(status).seriesId(seriesId).allDay(allDay);
        break;
      case "start":
        builder = new Builder(subject, (ZonedDateTime) newValue, end)
            .description(description).location(location)
            .status(status).seriesId(seriesId).allDay(allDay);
        break;
      case "end":
        builder = new Builder(subject, start, (ZonedDateTime) newValue)
            .description(description).location(location)
            .status(status).seriesId(seriesId).allDay(allDay);
        break;
      case "description":
        builder.description((String) newValue);
        break;
      case "location":
        builder.location((String) newValue);
        break;
      case "status":
        builder.status(EventStatus.valueOf(((String) newValue).toUpperCase()));
        break;
      case "seriesid":
        builder.seriesId((String) newValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }

    return builder.finalBuild();
  }

  /**
   * Compares this event with another based on start time, end time, and subject.
   *
   * @param other the event to compare
   * @return comparison result (-, 0, +)
   */
  @Override
  public int compareTo(Event other) {
    int cmp = this.start.compareTo(other.start);
    if (cmp != 0) {
      return cmp;
    }
    cmp = this.end.compareTo(other.end);
    if (cmp != 0) {
      return cmp;
    }
    return this.subject.compareToIgnoreCase(other.subject);
  }

  /**
   * Checks if this event is equal to another object.
   * Events are equal if they share subject, start, and end time.
   *
   * @param obj the object to compare
   * @return true if equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Event)) {
      return false;
    }
    Event other = (Event) obj;
    return subject.equalsIgnoreCase(other.subject)
        && start.equals(other.start)
        && end.equals(other.end);
  }

  /**
   * Computes a hash code consistent with {@link #equals(Object)}.
   *
   * @return hash code for this event
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject.toLowerCase(), start, end);
  }

  /**
   * Returns a human-readable string representation of the event.
   *
   * @return formatted event details
   */
  @Override
  public String toString() {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    return subject + " (" + start.format(fmt) + " - " + end.format(fmt) + ")";
  }

  /**
   * Checks if this event matches the given key.
   *
   * @param key event key to compare
   * @return true if subject, start, and end match
   */
  public boolean matchesKey(EventKey key) {
    return this.subject.equalsIgnoreCase(key.getSubject())
        && this.start.equals(key.getStart())
        && this.end.equals(key.getEnd());
  }

  /**
   * Checks if this event belongs to a given series.
   *
   * @param seriesId the ID to check
   * @return true if event belongs to that series
   */
  boolean belongsToSeries(String seriesId) {
    return this.seriesId != null && this.seriesId.equals(seriesId);
  }

  /**
   * Determines if this event should be modified based on edit mode and key.
   *
   * @param key  identifying key for the target event
   * @param mode edit mode (SINGLE, FROM_THIS_ONWARD, ENTIRE_SERIES)
   * @return true if the event qualifies for modification
   */
  boolean shouldModifyAccordingToMode(EventKey key, EditMode mode) {
    if (mode == null) {
      return false;
    }
    switch (mode) {
      case SINGLE:
        return this.matchesKey(key);
      case FROM_THIS_ONWARD:
        return this.seriesId != null
            && this.seriesId.equals(key.getSubject())
            && !this.start.isBefore(key.getStart());
      case ENTIRE_SERIES:
        return this.seriesId != null && this.seriesId.equals(key.getSubject());
      default:
        return false;
    }
  }

}

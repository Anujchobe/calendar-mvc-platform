package calendar.model;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Represents an immutable unique key for identifying a calendar event.
 *
 * <p>Each key is defined by its subject, start time, and end time.
 * Ensures that no two events with identical subject and times coexist in the calendar.</p>
 */
public final class EventKey {

  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;

  /**
   * Constructs an {@code EventKey} with subject, start, and end times.
   *
   * @param subject the event title (non-null and non-blank)
   * @param start   the event start time (non-null)
   * @param end     the event end time (non-null)
   * @throws IllegalArgumentException if any parameter is invalid
   */
  public EventKey(String subject, ZonedDateTime start, ZonedDateTime end) {
    if (subject == null || subject.isBlank()) {
      throw new IllegalArgumentException("EventKey subject cannot be null or blank");
    }
    if (start == null) {
      throw new IllegalArgumentException("EventKey start time cannot be null");
    }

    this.subject = subject;
    this.start = start;
    this.end = end;
  }

  /**
   * Returns the subject of the event.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the start time of the event.
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Returns the end time of the event.
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Checks equality based on subject, start, and end times (case-insensitive subject).
   *
   * @param obj the object to compare with
   * @return {@code true} if the two keys represent the same event
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EventKey)) {
      return false;
    }
    EventKey other = (EventKey) obj;
    return subject.equalsIgnoreCase(other.subject)
        && start.equals(other.start)
        && end.equals(other.end);
  }

  /**
   * Generates a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code of this key
   */
  @Override
  public int hashCode() {
    return Objects.hash(subject.toLowerCase(), start, end);
  }

  /**
   * Returns a readable string form of this key.
   *
   * @return a formatted string representing this event key
   */
  @Override
  public String toString() {
    return String.format("EventKey[%s, %s → %s]", subject, start, end);
  }
}

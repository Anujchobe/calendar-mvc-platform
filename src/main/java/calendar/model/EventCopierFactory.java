package calendar.model;

/**
 * Factory for creating {@link IeventCopier} instances.
 *
 * <p>Provides a centralized point for creating event copiers, supporting the
 * Dependency Inversion Principle by allowing clients to work with the
 * {@link IeventCopier} interface rather than concrete implementations.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Icalendar sourceCalendar = ...;
 * IeventCopier copier = EventCopierFactory.createCopier(sourceCalendar);
 * copier.copyEvent("Meeting", sourceTime, targetCalendar, targetTime);
 * }</pre>
 *
 * @see IeventCopier
 * @see EventCopier
 */
public class EventCopierFactory {

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private EventCopierFactory() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Creates an event copier for the specified source calendar.
   *
   * <p>The returned copier can copy events from the source calendar to any
   * target calendar, handling timezone conversions and property preservation
   * automatically.</p>
   *
   * @param sourceCalendar the calendar to copy events from; must not be null
   * @return a new {@link IeventCopier} instance configured for the source calendar
   * @throws IllegalArgumentException if sourceCalendar is null
   */
  public static IeventCopier createCopier(Icalendar sourceCalendar) {
    if (sourceCalendar == null) {
      throw new IllegalArgumentException("Source calendar cannot be null.");
    }
    return new EventCopier(sourceCalendar);
  }
}
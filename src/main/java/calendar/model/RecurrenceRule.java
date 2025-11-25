package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents recurrence rules for generating repeating events.
 *
 * <p>Supports repeating events for a fixed number of occurrences
 * or until a specific date, across selected weekdays.
 */
public final class RecurrenceRule {

  private final Set<DayOfWeek> weekdays;
  private final Integer occurrences;
  private final LocalDate until;

  /**
   * Constructs a recurrence rule.
   *
   * @param weekdays    days of the week the event repeats on (e.g., MONDAY, WEDNESDAY)
   * @param occurrences number of times to repeat (nullable if using 'until')
   * @param until       end date of repetition (nullable if using 'occurrences')
   */
  public RecurrenceRule(Set<DayOfWeek> weekdays, Integer occurrences, LocalDate until) {
    if ((occurrences == null && until == null)
        || (occurrences != null && until != null)) {
      throw new IllegalArgumentException("Specify either occurrences or until date, not both.");
    }
    this.weekdays = weekdays == null ? new HashSet<>() : Set.copyOf(weekdays);
    this.occurrences = occurrences;
    this.until = until;
  }

  /**
   * Generates a list of recurring event instances from a base event.
   *
   * @param seed the base (first) event
   * @return a list of generated Event instances
   */
  public List<Event> generateSeries(Event seed) {
    List<Event> result = new ArrayList<>();

    LocalDate seedDate = seed.getStart().toLocalDate();
    ZonedDateTime baseStart = seed.getStart();
    ZonedDateTime baseEnd = seed.getEnd();
    String seriesId = seed.getSeriesId() != null
        ? seed.getSeriesId()
        : UUID.randomUUID().toString();


    int count = 0;
    LocalDate date = seedDate;

    while (true) {
      DayOfWeek dow = date.getDayOfWeek();
      if (weekdays.contains(dow)) {
        ZonedDateTime start = baseStart.with(date);
        ZonedDateTime end = baseEnd.with(date);
        Event e = new Event.Builder(seed.getSubject(), start, end)
            .description(seed.getDescription())
            .location(seed.getLocation())
            .status(seed.getStatus())
            .seriesId(seriesId)
            .build();
        result.add(e);
        count++;
      }

      if (occurrences != null && count >= occurrences) {
        break;
      }
      date = date.plusDays(1);
      if (until != null && date.isAfter(until)) {
        break;
      }


    }

    return result;
  }

  public Set<DayOfWeek> getWeekdays() {
    return weekdays;
  }

  public Integer getOccurrences() {
    return occurrences;
  }

  public LocalDate getUntil() {
    return until;
  }
}

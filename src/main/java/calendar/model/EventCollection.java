package calendar.model;

import java.util.List;
import java.util.Map;

/**
 * Helper class to organize events into standalone and series categories.
 *
 * <p>Used internally by {@link EventCopier} to efficiently process mixed
 * collections of standalone and recurring events during bulk copy operations.</p>
 *
 * <p>This class is package-private as it's an implementation detail that
 * should not be exposed outside the model package.</p>
 *
 * @see EventCopier
 */
class EventCollection {
  private final List<Event> standaloneEvents;
  private final Map<String, List<Event>> seriesMap;

  /**
   * Constructs an EventCollection with categorized events.
   *
   * @param standaloneEvents list of standalone (non-recurring) events
   * @param seriesMap        map of series IDs to their respective event lists
   */
  EventCollection(List<Event> standaloneEvents,
                  Map<String, List<Event>> seriesMap) {
    this.standaloneEvents = standaloneEvents;
    this.seriesMap = seriesMap;
  }

  /**
   * Gets the standalone events.
   *
   * @return list of standalone events
   */
  List<Event> getStandaloneEvents() {
    return standaloneEvents;
  }

  /**
   * Gets the map of series events grouped by series ID.
   *
   * @return map of series IDs to event lists
   */
  Map<String, List<Event>> getSeriesMap() {
    return seriesMap;
  }
}
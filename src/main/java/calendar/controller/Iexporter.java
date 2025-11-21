package calendar.controller;

import calendar.model.Event;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents a generic exporter for calendar data.
 *
 * <p>Implementations of this interface define how calendar data
 * should be serialized into an external format such as CSV, JSON, or ICS.
 * This allows the calendar model to remain independent of specific export logic.</p>
 */
public interface Iexporter {

  /**
   * Exports a list of events to a specified file.
   *
   * @param events list of events to export
   * @param fileName output file name (platform independent)
   * @return path to the created export file
   */
  Path export(List<Event> events, String fileName);
}

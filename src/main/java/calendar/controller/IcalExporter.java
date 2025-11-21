package calendar.controller;

import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implements the Iexporter interface to export a list of events into the iCalendar (.ics) format
 * defined by RFC 5545.
 */
final class IcalExporter implements Iexporter {

  private final ZoneId zone;

  IcalExporter(ZoneId zone) {
    this.zone = zone;
  }

  /**
   * Exports the given list of events to an iCalendar file.
   *
   * @param events The list of events to export.
   * @param fileName The desired output file name.
   *                 If it lacks an .ics or .ical extension, one will be added.
   * @return The absolute path to the generated iCalendar file.
   * @throws RuntimeException if an IOException occurs during file writing.
   */
  @Override
  public Path export(List<Event> events, String fileName) {
    if (!(fileName.toLowerCase().endsWith(".ics") || fileName.toLowerCase().endsWith(".ical"))) {
      fileName += ".ics";
    }
    Path out = Path.of(fileName);

    final DateTimeFormatter icalFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\r\n");
    sb.append("VERSION:2.0\r\n");
    sb.append("PRODID:-//Virtual Calendar//EN\r\n");

    for (Event e : events) {
      final String dtStart = e.getStart().withZoneSameInstant(ZoneId.of(
          "UTC")).format(icalFmt);
      final String dtEnd   = e.getEnd().withZoneSameInstant(ZoneId.of(
          "UTC")).format(icalFmt);

      sb.append("BEGIN:VEVENT\r\n");
      sb.append("SUMMARY:").append(safe(e.getSubject())).append("\r\n");

      if (e.getDescription() != null) {
        sb.append("DESCRIPTION:").append(safe(e.getDescription())).append("\r\n");
      }

      if (e.getLocation() != null) {
        sb.append("LOCATION:").append(safe(e.getLocation())).append("\r\n");
      }

      sb.append("DTSTART:").append(dtStart).append("\r\n");
      sb.append("DTEND:").append(dtEnd).append("\r\n");
      sb.append("END:VEVENT\r\n");
    }

    sb.append("END:VCALENDAR\r\n");

    try {
      Files.writeString(out, sb.toString());
    } catch (IOException ex) {
      throw new RuntimeException("iCal export failed: " + ex.getMessage(), ex);
    }
    return out.toAbsolutePath();
  }

  /**
   * Performs basic escaping on a string for iCalendar format compatibility.
   *
   * @param s The string to be escaped.
   * @return The escaped string.
   */
  private String safe(String s) {
    return s.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,").replace(";", "\\;");
  }
}
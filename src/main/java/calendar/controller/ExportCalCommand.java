package calendar.controller;

import calendar.model.Event;
import calendar.model.Icalendar;
import calendar.model.IcalendarManager;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;

/**
 * Command to export all events in the active calendar.
 *
 * <p>Usage:</p>
 * <pre>
 * export calendar my_calendar.csv
 * export calendar semester_schedule.ical
 * </pre>
 *
 * <p>Automatically detects format based on file extension:
 * <ul>
 *   <li>.csv → exported via {@link CsvExporter}</li>
 *   <li>.ics / .ical → exported via {@link IcalExporter}</li>
 * </ul>
 */
public class ExportCalCommand extends AbstractCommand {

  /**
   * Constructs the export calendar command with the provided arguments.
   *
   * @param args The list of string arguments, typically specifying the output file name or path.
   */
  public ExportCalCommand(List<String> args) {
    super(args);
  }

  @Override
  public void execute(IcalendarManager manager) {
    if (manager == null) {
      throw new IllegalArgumentException("Calendar manager cannot be null.");
    }

    Icalendar cal = manager.getActiveCalendar();
    if (cal == null) {
      throw new IllegalStateException(
          "No active calendar selected. Use 'use calendar --name <name>' first.");
    }

    ensureArgCountAtLeast(1,
        "Usage: export calendar <filename.csv|filename.ical|filename.ics>");
    String fileName = args.get(0).trim().toLowerCase();

    List<Event> events = cal.getAllEvents();
    if (events.isEmpty()) {
      System.out.println("(no events to export)");
      return;
    }

    Iexporter exporter;
    if (fileName.endsWith(".csv")) {
      exporter = new CsvExporter();
    } else if (fileName.endsWith(".ics") || fileName.endsWith(".ical")) {
      ZoneId zone = cal.getZone();
      exporter = new IcalExporter(zone);
    } else {
      throw new IllegalArgumentException("Unsupported format. Use .csv or .ical/.ics extension.");
    }

    Path path = exporter.export(events, fileName);
    System.out.println("Exported calendar to: " + path.toAbsolutePath());
  }
}

package calendar.controller;

import calendar.model.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class CsvExporter implements Iexporter {

  @Override
  public Path export(List<Event> events, String fileName) {
    if (!fileName.toLowerCase().endsWith(".csv")) {
      fileName += ".csv";
    }
    Path out = Path.of(fileName);

    StringBuilder sb = new StringBuilder();
    sb.append("Subject,Start,End,Description,Location,Status,AllDay,SeriesId\n");
    for (Event e : events) {
      sb.append(escape(e.getSubject())).append(',')
          .append(e.getStart()).append(',')
          .append(e.getEnd()).append(',')
          .append(escape(e.getDescription())).append(',')
          .append(escape(e.getLocation())).append(',')
          .append(e.getStatus()).append(',')
          .append(e.isAllDay()).append(',')
          .append(escape(e.getSeriesId()))
          .append('\n');
    }

    try {
      Files.writeString(out, sb.toString());
    } catch (IOException ex) {
      throw new RuntimeException("CSV export failed: " + ex.getMessage(), ex);
    }
    return out.toAbsolutePath();
  }

  private String escape(String s) {
    if (s == null) {
      return "";
    }
    String v = s.replace("\"", "\"\"");
    return '"' + v + '"';
  }
}

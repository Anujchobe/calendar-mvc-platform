package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Read-only modal dialog displaying all events for a specific date.
 *
 * <p><b>Purpose:</b></p>
 * <ul>
 *     <li>Provides a quick overview of all events on a selected date</li>
 *     <li>Formats events in a readable list with times and locations</li>
 *     <li>Scrollable for dates with many events</li>
 * </ul>
 *
 * <p><b>Design:</b></p>
 * <ul>
 *     <li>Read-only - no editing capabilities</li>
 *     <li>Uses list model for efficient rendering</li>
 *     <li>Displays empty state message when no events exist</li>
 * </ul>
 */
public class ViewEventsDialog extends AbstractViewDialog {

  /**
   * Formatter for displaying times (e.g., "14:30").
   */
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Constructs a view-only events dialog for the specified date.
   *
   * @param parent parent frame
   * @param date   the date being viewed
   * @param events list of events scheduled on that date
   */
  public ViewEventsDialog(JFrame parent, LocalDate date, List<Event> events) {
    super(parent, "Events on " + date.toString());

    setSize(450, 400);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    initEventsList(events);
    initCloseButton();
  }

  /**
   * Creates a scrollable list displaying all events.
   *
   * <p>Each event is formatted as:
   * "Subject | HH:mm - HH:mm | Location | Status"</p>
   *
   * @param events list of events to display
   */
  private void initEventsList(List<Event> events) {
    DefaultListModel<String> model = new DefaultListModel<>();

    if (events.isEmpty()) {
      model.addElement("No events scheduled for this date.");
    } else {
      for (Event e : events) {
        String entry = String.format(
            "%s | %s - %s | %s | %s",
            e.getSubject(),
            e.getStart().format(TIME_FORMAT),
            e.getEnd().format(TIME_FORMAT),
            e.getLocation().isEmpty() ? "No location" : e.getLocation(),
            e.getStatus()
        );
        model.addElement(entry);
      }
    }

    JList<String> list = new JList<>(model);
    JScrollPane scroll = new JScrollPane(list);
    scroll.setPreferredSize(new Dimension(420, 300));

    add(scroll, BorderLayout.CENTER);
  }

  /**
   * Adds a close button to dismiss the dialog.
   */
  private void initCloseButton() {
    JButton close = new JButton("Close");
    close.addActionListener(e -> dispose());

    JPanel panel = new JPanel();
    panel.add(close);

    add(panel, BorderLayout.SOUTH);
  }
}
package calendar.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * Panel that displays the list of calendars available in the application.
 *
 * <p>This component is responsible only for showing calendar names and notifying
 * a registered listener when the user selects one of them. It does not interact
 * with the model directly, which keeps the design modular and easy to reuse.</p>
 *
 * <p>The panel uses a {@link JList} backed by a {@link DefaultListModel}. A
 * simple callback interface allows the parent view or controller to respond to
 * user selections.</p>
 */
public class CalendarListPanel extends JPanel {

  /**
   * Backing model for storing and updating the calendar names displayed in the list.
   */
  private final DefaultListModel<String> listModel = new DefaultListModel<>();

  /**
   * The visible list of calendar names.
   */
  private final JList<String> calendarList = new JList<>(listModel);

  /**
   * Listener invoked when the user selects a calendar.
   */
  private CalendarSelectionListener listener;

  /**
   * Ensures only one listener is attached at a time.
   */
  private boolean listenerAttached = false;

  /**
   * Functional interface for selection callbacks.
   */
  @FunctionalInterface
  public interface CalendarSelectionListener {

    /**
     * Called when the user selects a calendar from the list.
     *
     * @param calendarName the chosen calendar name
     */
    void onCalendarSelected(String calendarName);
  }

  /**
   * Creates a new calendar list panel.
   *
   * <p>Sets a fixed width, enables single selection mode, and wraps
   * the list in a scrollable container. A title is added at the top.</p>
   */
  public CalendarListPanel() {
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(200, 0));

    calendarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    calendarList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && listener != null) {
        String selected = calendarList.getSelectedValue();
        if (selected != null) {
          listener.onCalendarSelected(selected);
        }
      }
    });

    JScrollPane scroll = new JScrollPane(calendarList);
    add(new JLabel("Calendars", SwingConstants.CENTER), BorderLayout.NORTH);
    add(scroll, BorderLayout.CENTER);
  }

  /**
   * Replaces the existing calendar list with a new set of names.
   *
   * <p>If the new list is not empty, the first calendar is selected.
   * This may trigger the listener if one is attached.</p>
   *
   * @param calendars the list of calendar names to display
   */
  public void setCalendars(List<String> calendars) {
    listModel.clear();

    for (String name : calendars) {
      listModel.addElement(name);
    }

    if (!calendars.isEmpty()) {
      calendarList.setSelectedIndex(0);
    }
  }

  /**
   * Programmatically selects a calendar by name.
   *
   * @param calendarName the name to highlight
   */
  public void setSelectedCalendar(String calendarName) {
    calendarList.setSelectedValue(calendarName, true);
  }

  /**
   * Registers the listener to be notified when the user selects a calendar.
   *
   * @param listener the callback to attach
   */
  public void setSelectionListener(CalendarSelectionListener listener) {
    if (listenerAttached) {
      return;
    }
    this.listener = listener;
    listenerAttached = true;
  }

  /**
   * Returns the currently selected calendar.
   *
   * @return the selected calendar name, or {@code null} if none is selected
   */
  public String getSelectedCalendar() {
    return calendarList.getSelectedValue();
  }
}

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
 * Reusable panel component for displaying and selecting calendars.
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Display list of available calendars</li>
 *     <li>Highlight the currently active calendar</li>
 *     <li>Fire selection change events to registered listeners</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *     <li><b>Single Responsibility:</b> Only handles calendar selection UI</li>
 *     <li><b>Reusability:</b> Can be embedded in any view that needs
 *         calendar selection</li>
 *     <li><b>Observer Pattern:</b> Uses listener callback for decoupling</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * CalendarListPanel panel = new CalendarListPanel();
 * panel.setCalendars(List.of("Work", "Personal", "Family"));
 * panel.setSelectionListener(calName -> {
 *     features.useCalendar(calName);
 * });
 * }</pre>
 */
public class CalendarListPanel extends JPanel {

  /** List model for managing calendar names. */
  private final DefaultListModel<String> listModel = new DefaultListModel<>();

  /** JList component for displaying calendars. */
  private final JList<String> calendarList = new JList<>(listModel);

  /** Listener for selection changes. */
  private CalendarSelectionListener listener;

  /**
   * Functional interface for calendar selection callbacks.
   *
   * <p>Allows parent views to react to calendar selection changes
   * without tight coupling.</p>
   */
  @FunctionalInterface
  public interface CalendarSelectionListener {
    /**
     * Called when a calendar is selected by the user.
     *
     * @param calendarName the name of the selected calendar
     */
    void onCalendarSelected(String calendarName);
  }

  /**
   * Constructs a calendar list panel with default styling.
   *
   * <p>Configures:</p>
   * <ul>
   *     <li>Preferred width of 200 pixels</li>
   *     <li>Single selection mode</li>
   *     <li>Scrollable list</li>
   *     <li>Title label</li>
   * </ul>
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
   * Updates the list of available calendars.
   *
   * <p>Clears the existing list and repopulates it with the provided names.
   * Automatically selects the first calendar if available.</p>
   *
   * @param calendars list of calendar names to display
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
   * Sets the active calendar by name.
   *
   * <p>Programmatically selects the specified calendar in the list.</p>
   *
   * @param calendarName name of the calendar to select
   */
  public void setSelectedCalendar(String calendarName) {
    calendarList.setSelectedValue(calendarName, true);
  }

  /**
   * Registers a listener for selection change events.
   *
   * <p>The listener will be notified whenever the user selects a
   * different calendar from the list.</p>
   *
   * @param listener callback for selection events
   */
  public void setSelectionListener(CalendarSelectionListener listener) {
    this.listener = listener;
  }

  /**
   * Gets the currently selected calendar name.
   *
   * @return selected calendar name, or null if none selected
   */
  public String getSelectedCalendar() {
    return calendarList.getSelectedValue();
  }
}

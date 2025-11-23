package calendar.view;

import calendar.controller.Ifeatures;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * Concrete Swing-based implementation of {@link Icalendarview} serving as
 * the main application window.
 *
 * <p><b>Architecture: View as Event Firer, Controller as Listener</b></p>
 * <ul>
 *     <li><b>View fires events:</b> All user actions fire to controller via
 *         {@code features.methodName()}</li>
 *     <li><b>Controller listens:</b> Controller implements {@link Ifeatures}
 *         and receives all events</li>
 *     <li><b>Zero model access:</b> View NEVER calls model methods directly</li>
 *     <li><b>Dialogs are dumb:</b> Dialogs only collect data, view fires events</li>
 * </ul>
 */
public class SwingCalendarView extends JFrame implements Icalendarview {

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");

  private MonthViewPanel monthViewPanel;
  private CalendarListPanel calendarListPanel;
  private JLabel monthLabel;
  private DefaultListModel<String> eventListModel;
  private JList<String> eventList;
  private JButton createCalendarBtn;
  private JButton createEventBtn;
  private JButton createRecurringBtn;
  private JButton editEventBtn;
  private JButton viewEventsBtn;
  private YearMonth currentMonth = YearMonth.now();
  private LocalDate selectedDate = LocalDate.now();
  private Ifeatures features;  // ONLY reference to controller (listener)
  private List<Event> currentEvents;

  /**
   * Constructs the main application window.
   */
  public SwingCalendarView() {
    super("Virtual Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1100, 700);
    setLayout(new BorderLayout(10, 10));
    initializeUi();
  }

  /**
   * Initializes all UI components.
   */
  private void initializeUi() {
    initTopToolbar();
    initCalendarSidebar();
    initCenterPanel();
    initEventSidebar();
  }

  /**
   * Creates top toolbar with calendar creation button.
   */
  private void initTopToolbar() {
    JPanel toolbar = new JPanel(new BorderLayout(10, 0));
    toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    createCalendarBtn = new JButton("+ New Calendar");
    createCalendarBtn.addActionListener(e -> openCreateCalendarDialog());

    JLabel title = new JLabel("📅 Virtual Calendar", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 18));

    toolbar.add(createCalendarBtn, BorderLayout.WEST);
    toolbar.add(title, BorderLayout.CENTER);

    add(toolbar, BorderLayout.NORTH);
  }

  /**
   * Creates left sidebar with calendar selection panel.
   *
   * <p>Registers listener that FIRES calendar switch events to controller.</p>
   */
  private void initCalendarSidebar() {
    calendarListPanel = new CalendarListPanel();
    calendarListPanel.setSelectionListener(calendarName -> {
      if (features != null) {
        // FIRE EVENT to controller listener
        features.useCalendar(calendarName);
        refresh();
      }
    });

    add(calendarListPanel, BorderLayout.WEST);
  }

  /**
   * Creates center panel with month navigation and grid.
   */
  private void initCenterPanel() {
    JPanel center = new JPanel(new BorderLayout(10, 10));
    center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel nav = createNavigationPanel();
    center.add(nav, BorderLayout.NORTH);

    monthViewPanel = new MonthViewPanel(currentMonth);
    monthViewPanel.addDateSelectionListener(date -> {
      selectedDate = date;

      if (features != null) {
        // FIRE correct event to controller listener
        features.selectDate(date);     // ← NEW
        loadEventsForSelectedDate();   // refresh event list
      }
    });

    center.add(monthViewPanel, BorderLayout.CENTER);

    add(center, BorderLayout.CENTER);
  }

  /**
   * Creates navigation panel with month traversal controls.
   */
  private JPanel createNavigationPanel() {
    final JPanel nav = new JPanel(new BorderLayout(10, 0));

    final JButton prevBtn = new JButton("◀ Previous");
    final JButton nextBtn = new JButton("Next ▶");
    final JButton todayBtn = new JButton("Today");

    monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 20));
    updateMonthLabel();

    prevBtn.addActionListener(e -> navigateToPreviousMonth());
    nextBtn.addActionListener(e -> navigateToNextMonth());
    todayBtn.addActionListener(e -> navigateToToday());

    JPanel leftPanel = new JPanel(new GridLayout(1, 2, 5, 0));
    leftPanel.add(prevBtn);
    leftPanel.add(todayBtn);

    nav.add(leftPanel, BorderLayout.WEST);
    nav.add(monthLabel, BorderLayout.CENTER);
    nav.add(nextBtn, BorderLayout.EAST);

    return nav;
  }

  /**
   * Creates right sidebar with event list and action buttons.
   */
  private void initEventSidebar() {
    JPanel sidebar = new JPanel(new BorderLayout(5, 5));
    sidebar.setPreferredSize(new Dimension(280, 0));
    sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel title = new JLabel("Events on Selected Date", SwingConstants.CENTER);
    title.setFont(new Font("Arial", Font.BOLD, 14));
    sidebar.add(title, BorderLayout.NORTH);

    eventListModel = new DefaultListModel<>();
    eventList = new JList<>(eventListModel);
    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane scroll = new JScrollPane(eventList);
    sidebar.add(scroll, BorderLayout.CENTER);

    JPanel buttonPanel = createEventActionButtons();
    sidebar.add(buttonPanel, BorderLayout.SOUTH);

    add(sidebar, BorderLayout.EAST);
  }

  /**
   * Creates event action button panel.
   *
   * <p>Note: Delete functionality NOT included as it's not in
   * assignment requirements (Section 1.2 specifies: create, edit, view)</p>
   */
  private JPanel createEventActionButtons() {
    final JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));

    createEventBtn = new JButton("Create Event");
    createRecurringBtn = new JButton("Create Recurring");
    editEventBtn = new JButton("Edit Event");
    viewEventsBtn = new JButton("View All Events");

    createEventBtn.addActionListener(e -> openCreateEventDialog());
    createRecurringBtn.addActionListener(e -> openCreateRecurringDialog());
    editEventBtn.addActionListener(e -> openEditEventDialog());
    viewEventsBtn.addActionListener(e -> openViewEventsDialog());

    panel.add(createEventBtn);
    panel.add(createRecurringBtn);
    panel.add(editEventBtn);
    panel.add(viewEventsBtn);

    return panel;
  }


  /**
   * Navigates to previous month.
   *
   * <p>FIRES navigation event to controller listener.</p>
   */
  private void navigateToPreviousMonth() {
    currentMonth = currentMonth.minusMonths(1);
    if (features != null) {
      features.navigateToMonth(currentMonth);  // FIRE EVENT
    }
    updateMonthView();
  }

  /**
   * Navigates to next month.
   *
   * <p>FIRES navigation event to controller listener.</p>
   */
  private void navigateToNextMonth() {
    currentMonth = currentMonth.plusMonths(1);
    if (features != null) {
      features.navigateToMonth(currentMonth);  // FIRE EVENT
    }
    updateMonthView();
  }

  /**
   * Navigates to today's date.
   *
   * <p>FIRES navigation event to controller listener.</p>
   */
  private void navigateToToday() {
    LocalDate today = LocalDate.now();
    currentMonth = YearMonth.from(today);
    selectedDate = today;

    if (features != null) {
      features.navigateToMonth(currentMonth);  // tell controller about month change
      features.selectDate(today);              // tell controller the day selected
    }

    updateMonthView();
    loadEventsForSelectedDate();
  }


  /**
   * Updates month view panel.
   */
  private void updateMonthView() {
    updateMonthLabel();
    monthViewPanel.setMonth(currentMonth);
    monthViewPanel.setSelectedDate(selectedDate);
  }

  /**
   * Updates month label text.
   */
  private void updateMonthLabel() {
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
  }

  /**
   * Loads events for selected date.
   *
   * <p>FIRES query event to controller listener to get events.</p>
   */
  private void loadEventsForSelectedDate() {
    if (features == null) {
      return;
    }

    currentEvents = features.getEventsOn(selectedDate);  // FIRE EVENT
    displayEventsInList(currentEvents);
  }

  /**
   * Displays events in the sidebar list.
   */
  private void displayEventsInList(List<Event> events) {
    eventListModel.clear();

    if (events.isEmpty()) {
      eventListModel.addElement("No events on this date");
      return;
    }

    for (Event event : events) {
      String display = String.format("%s (%s - %s)",
          event.getSubject(),
          event.getStart().format(TIME_FORMAT),
          event.getEnd().format(TIME_FORMAT));
      eventListModel.addElement(display);
    }
  }


  /**
   * Opens create calendar dialog.
   *
   * <p>Flow: Opens dialog → Extracts data → FIRES to controller listener</p>
   */
  private void openCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this);
    dialog.setVisible(true);

    if (dialog.isConfirmed() && features != null) {
      try {
        // FIRE EVENT to controller listener
        features.createCalendar(
            dialog.getCalendarName(),
            dialog.getTimezone()
        );
        refreshCalendarList();
        showSuccess("Calendar created successfully");
      } catch (Exception ex) {
        showError("Failed to create calendar: " + ex.getMessage());
      }
    }
  }

  /**
   * Opens create event dialog.
   *
   * <p>Flow: Opens dialog → Extracts data → FIRES to controller listener</p>
   */
  private void openCreateEventDialog() {
    CreateEventDialog dialog = new CreateEventDialog(this, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed() && features != null) {
      try {
        // FIRE EVENT to controller listener
        features.createSingleEvent(
            dialog.getSubject(),
            dialog.getStart(),
            dialog.getEnd(),
            dialog.getDescription(),
            dialog.getEventLocation(),
            dialog.getStatus()
        );
        loadEventsForSelectedDate();
        showSuccess("Event created successfully");
      } catch (Exception ex) {
        showError("Failed to create event: " + ex.getMessage());
      }
    }
  }

  /**
   * Opens recurring event dialog.
   *
   * <p>Flow: Opens dialog → Extracts data → FIRES to controller listener</p>
   */
  private void openCreateRecurringDialog() {
    RecurringEventDialog dialog = new RecurringEventDialog(this, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed() && features != null) {
      try {
        Integer occurrences = dialog.useOccurrences()
            ?
            dialog.getOccurrences() : null;
        LocalDate endDate = !dialog.useOccurrences()
            ?
            dialog.getEndDate() : null;

        // FIRE EVENT to controller listener
        features.createRecurringEvent(
            dialog.getSubject(),
            dialog.getStart(),
            dialog.getEnd(),
            dialog.getDescription(),
            dialog.getEventLocation(),
            dialog.getStatus(),
            dialog.getWeekdayPattern(),
            occurrences,
            endDate
        );
        loadEventsForSelectedDate();
        showSuccess("Recurring event created successfully");
      } catch (Exception ex) {
        showError("Failed to create recurring event: " + ex.getMessage());
      }
    }
  }

  /**
   * Opens edit event dialog.
   *
   * <p>Supports three modes:
   * <ul>
   *     <li>Single event edit</li>
   *     <li>Edit all events with same subject</li>
   *     <li>Edit events from specific date onward</li>
   * </ul>
   *
   * <p>FIRES appropriate edit event to controller listener based on mode.</p>
   */
  private void openEditEventDialog() {
    int selectedIndex = eventList.getSelectedIndex();
    if (selectedIndex < 0 || currentEvents == null
        ||
        currentEvents.isEmpty()) {
      showError("Please select an event to edit");
      return;
    }

    Event selectedEvent = currentEvents.get(selectedIndex);
    EditEventDialog dialog = new EditEventDialog(this, selectedEvent);
    dialog.setVisible(true);

    if (dialog.isConfirmed() && features != null) {
      try {
        if (dialog.isEditMultiple()) {
          // Batch edit mode
          if (dialog.isFromDateEnabled()) {
            // FIRE EVENT: Edit series from date onward
            editSeriesProperties(
                selectedEvent.getSubject(),
                selectedEvent.getStart(),
                selectedEvent.getEnd(),
                dialog,
                true
            );
          } else {
            // FIRE EVENT: Edit entire series
            editSeriesProperties(
                selectedEvent.getSubject(),
                selectedEvent.getStart(),
                selectedEvent.getEnd(),
                dialog,
                false
            );
          }
        } else {
          // FIRE EVENT: Edit single event
          editSingleEventProperties(
              selectedEvent.getSubject(),
              selectedEvent.getStart(),
              selectedEvent.getEnd(),
              dialog
          );
        }
        loadEventsForSelectedDate();
        showSuccess("Event updated successfully");
      } catch (Exception ex) {
        showError("Failed to update event: " + ex.getMessage());
      }
    }
  }

  /**
   * Helper method to fire single event edit to controller.
   *
   * <p>FIRES multiple property updates to controller listener.</p>
   */
  private void editSingleEventProperties(String originalSubject,
                                         ZonedDateTime originalStart,
                                         ZonedDateTime originalEnd,
                                         EditEventDialog dialog) {
    // Update subject
    features.editSingleEvent(originalSubject, originalStart, originalEnd,
        "subject", dialog.getSubject());

    // Update other properties using new subject
    features.editSingleEvent(dialog.getSubject(), originalStart, originalEnd,
        "location", dialog.getLocation());
    features.editSingleEvent(dialog.getSubject(), originalStart, originalEnd,
        "description", dialog.getDescription());
    features.editSingleEvent(dialog.getSubject(), originalStart, originalEnd,
        "start", dialog.getStart());
    features.editSingleEvent(dialog.getSubject(), dialog.getStart(), originalEnd,
        "end", dialog.getEnd());
    features.editSingleEvent(dialog.getSubject(), dialog.getStart(), dialog.getEnd(),
        "status", dialog.getStatus().name());
  }

  /**
   * Helper method to fire series edit to controller.
   *
   * <p>FIRES multiple property updates to controller listener.</p>
   *
   * @param fromThisOnward true for FROM_THIS_ONWARD, false for ENTIRE_SERIES
   */
  private void editSeriesProperties(String originalSubject,
                                    ZonedDateTime originalStart,
                                    ZonedDateTime originalEnd,
                                    EditEventDialog dialog,
                                    boolean fromThisOnward) {
    String newSubject = dialog.getSubject();
    String newLocation = dialog.getEventLocation();
    String newDescription = dialog.getDescription();
    String newStatus = dialog.getStatus().name();

    if (fromThisOnward) {
      // EDIT SERIES: FROM THIS ONWARD
      features.editSeriesFromThisOnward(
          originalSubject, originalStart, originalEnd,
          "subject", newSubject
      );

      // Use new subject for subsequent edits
      features.editSeriesFromThisOnward(
          newSubject, originalStart, originalEnd,
          "location", newLocation
      );

      features.editSeriesFromThisOnward(
          newSubject, originalStart, originalEnd,
          "description", newDescription
      );

      features.editSeriesFromThisOnward(
          newSubject, originalStart, originalEnd,
          "status", newStatus
      );

    } else {
      // EDIT ENTIRE SERIES
      features.editEntireSeries(
          originalSubject, originalStart, originalEnd,
          "subject", newSubject
      );

      features.editEntireSeries(
          newSubject, originalStart, originalEnd,
          "location", newLocation
      );

      features.editEntireSeries(
          newSubject, originalStart, originalEnd,
          "description", newDescription
      );

      features.editEntireSeries(
          newSubject, originalStart, originalEnd,
          "status", newStatus
      );
    }
  }

  /**
   * Opens view events dialog (read-only).
   */
  private void openViewEventsDialog() {
    if (features == null) {
      return;
    }

    List<Event> events = features.getEventsOn(selectedDate);  // FIRE EVENT
    ViewEventsDialog dialog = new ViewEventsDialog(this, selectedDate, events);
    dialog.setVisible(true);
  }


  @Override
  public void display() {
    setVisible(true);
  }

  @Override
  public void refresh() {
    updateMonthView();
    loadEventsForSelectedDate();
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(
        this,
        message,
        "Error",
        JOptionPane.ERROR_MESSAGE
    );
  }

  @Override
  public void showSuccess(String message) {
    JOptionPane.showMessageDialog(
        this,
        message,
        "Success",
        JOptionPane.INFORMATION_MESSAGE
    );
  }

  /**
   * Registers controller as listener for all view events.
   *
   * <p>This is the Observer pattern registration:
   * <ul>
   *     <li>View is the Subject (fires events)</li>
   *     <li>Controller is the Observer (listens)</li>
   *     <li>This method performs the registration</li>
   * </ul>
   *
   * @param features controller interface that will listen to all events
   */
  @Override
  public void setFeatures(Ifeatures features) {
    this.features = features;  // Register controller as listener
    refreshCalendarList();
    loadEventsForSelectedDate();
  }

  /**
   * Refreshes calendar list from controller.
   *
   * <p>FIRES query event to controller listener.</p>
   */
  private void refreshCalendarList() {
    if (features != null) {
      List<String> calendarNames = features.getCalendarNames();  // FIRE EVENT
      calendarListPanel.setCalendars(calendarNames);

      if (!calendarNames.isEmpty()) {
        calendarListPanel.setSelectedCalendar(calendarNames.get(0));
      }
    }
  }
}
package calendar.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 * Reusable calendar grid panel displaying a month view with day selection.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>7x7 grid layout (weekday headers + day buttons)</li>
 *     <li>Highlights current date with blue border</li>
 *     <li>Highlights selected date with blue background</li>
 *     <li>Fires date selection events to registered listeners</li>
 *     <li>Handles month transitions gracefully</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *     <li><b>Open/Closed:</b> Can be reused in different views without
 *         modification</li>
 *     <li><b>Observer Pattern:</b> Uses listeners for loose coupling</li>
 *     <li><b>Single Responsibility:</b> Only handles month grid rendering
 *         and date selection</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * MonthViewPanel monthPanel = new MonthViewPanel(YearMonth.now());
 * monthPanel.addDateSelectionListener(date -> {
 *     features.selectDate(date);
 *     loadEventsForDate(date);
 * });
 * }</pre>
 */
public class MonthViewPanel extends JPanel {

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private LocalDate today = LocalDate.now();

  /** List of registered date selection listeners. */
  private final List<DateSelectionListener> listeners = new ArrayList<>();

  /** 6x7 grid of day buttons. */
  private final JButton[][] dayButtons = new JButton[6][7];

  /**
   * Functional interface for date selection callbacks.
   */
  @FunctionalInterface
  public interface DateSelectionListener {
    /**
     * Called when a user clicks on a date in the month view.
     *
     * @param date the date that was selected
     */
    void onDateSelected(LocalDate date);
  }

  /**
   * Constructs a month view panel for the specified month.
   *
   * <p>Initializes the 7x7 grid and renders the specified month.</p>
   *
   * @param month initial month to display
   */
  public MonthViewPanel(YearMonth month) {
    this.currentMonth = month;
    this.selectedDate = month.atDay(1);

    setLayout(new GridLayout(7, 7, 3, 3));
    setPreferredSize(new Dimension(600, 500));

    initializeGrid();
    renderMonth();
  }

  /**
   * Creates the 7x7 button grid with weekday headers.
   *
   * <p>First row contains day-of-week labels, remaining 6 rows
   * contain clickable day buttons.</p>
   */
  private void initializeGrid() {
    String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    // Add weekday header labels
    for (String day : weekdays) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      add(label);
    }

    // Create day buttons
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        dayButtons[row][col] = btn;
        add(btn);

        final int r = row;
        final int c = col;
        btn.addActionListener(e -> handleDayClick(r, c));
      }
    }
  }

  /**
   * Renders the current month's days in the grid.
   *
   * <p>Algorithm:</p>
   * <ol>
   *     <li>Calculate offset for first day of month</li>
   *     <li>Fill empty cells before month starts</li>
   *     <li>Populate actual days of the month</li>
   *     <li>Apply visual styling (borders, backgrounds)</li>
   *     <li>Fill empty cells after month ends</li>
   * </ol>
   */
  private void renderMonth() {
    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
    int daysInMonth = currentMonth.lengthOfMonth();

    int day = 1;
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        JButton btn = dayButtons[row][col];

        if (row == 0 && col < startDayOfWeek) {
          // Empty cell before month starts
          btn.setText("");
          btn.setEnabled(false);
        } else if (day <= daysInMonth) {
          btn.setText(String.valueOf(day));
          btn.setEnabled(true);

          // Reset styling
          btn.setBackground(Color.WHITE);
          btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
          LocalDate date = currentMonth.atDay(day);
          // Highlight today's date
          if (date.equals(today)) {
            btn.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
          }

          // Highlight selected date
          if (date.equals(selectedDate)) {
            btn.setBackground(new Color(180, 220, 255));
          }

          day++;
        } else {
          // Empty cell after month ends
          btn.setText("");
          btn.setEnabled(false);
        }
      }
    }

    revalidate();
    repaint();
  }

  /**
   * Handles clicks on day buttons.
   *
   * <p>Extracts the day number from the button text, updates the
   * selected date, re-renders the grid, and notifies all listeners.</p>
   *
   * @param row row index of clicked button
   * @param col column index of clicked button
   */
  private void handleDayClick(int row, int col) {
    JButton btn = dayButtons[row][col];
    String text = btn.getText();

    if (!text.isEmpty()) {
      int day = Integer.parseInt(text);
      selectedDate = currentMonth.atDay(day);
      renderMonth();
      notifyListeners(selectedDate);
    }
  }

  /**
   * Notifies all registered listeners of a date selection.
   *
   * @param date the date that was selected
   */
  private void notifyListeners(LocalDate date) {
    for (DateSelectionListener listener : listeners) {
      listener.onDateSelected(date);
    }
  }

  /**
   * Changes the displayed month and re-renders the grid.
   *
   * @param month new month to display
   */
  public void setMonth(YearMonth month) {
    this.currentMonth = month;
    renderMonth();
  }

  /**
   * Sets the selected date and updates the visual highlighting.
   *
   * @param date date to select
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    renderMonth();
  }

  /**
   * Registers a date selection listener.
   *
   * @param listener callback to invoke when dates are selected
   */
  public void addDateSelectionListener(DateSelectionListener listener) {
    listeners.add(listener);
  }

  /**
   * Returns the currently displayed month.
   *
   * @return current month
   */
  public YearMonth getCurrentMonth() {
    return currentMonth;
  }

  /**
   * Returns the currently selected date.
   *
   * @return selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }
}
package calendar.view;

import calendar.model.EventStatus;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;


/**
 * Modal dialog for creating recurring events with weekday selection
 * and recurrence rules.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Standard event fields (subject, location, description, etc.)</li>
 *     <li>Weekday checkboxes (Monday through Sunday)</li>
 *     <li>Choice between number of occurrences or end date</li>
 *     <li>Radio buttons to select recurrence termination method</li>
 * </ul>
 *
 */
public class RecurringEventDialog extends AbstractViewDialog {

  private final JTextField subjectField = new JTextField();
  private final JTextField locationField = new JTextField();
  private final JTextArea descriptionArea = new JTextArea(3, 20);
  private final JComboBox<EventStatus> statusCombo =
      new JComboBox<>(EventStatus.values());

  private final JSpinner startSpinner;
  private final JSpinner endSpinner;

  /**
   * Array of checkboxes for weekday selection (Mon-Sun).
   */
  private final JCheckBox[] weekdayBoxes = new JCheckBox[7];

  /**
   * Labels for weekdays.
   */
  private final String[] weekdayNames = {"Monday", "Tuesday", "Wednesday",
      "Thursday", "Friday", "Saturday", "Sunday"};

  /**
   * Radio button for terminating by occurrence count.
   */
  private final JRadioButton occurrencesRadio =
      new JRadioButton("Number of occurrences:", true);

  /**
   * Radio button for terminating by end date.
   */
  private final JRadioButton endDateRadio =
      new JRadioButton("Until end date:");

  /**
   * Spinner for selecting number of occurrences.
   */
  private final JSpinner occurrencesSpinner =
      new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));

  /**
   * Spinner for selecting end date.
   */
  private final JSpinner endDateSpinner;

  private boolean confirmed = false;
  private final LocalDate date;

  /**
   * Constructs a recurring event dialog for the specified date.
   *
   * @param parent parent frame
   * @param date   date for the first occurrence of the event
   */
  public RecurringEventDialog(JFrame parent, LocalDate date) {
    super(parent, "Create Recurring Event");
    this.date = date;

    setSize(500, 600);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    startSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());

    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));
    endDateSpinner.setEditor(
        new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));

    initFormPanel();
    initRecurrencePanel();
    initButtonPanel();

    // Radio button group and enable/disable logic
    ButtonGroup bg = new ButtonGroup();
    bg.add(occurrencesRadio);
    bg.add(endDateRadio);

    occurrencesRadio.addActionListener(e -> {
      occurrencesSpinner.setEnabled(true);
      endDateSpinner.setEnabled(false);
    });
    endDateRadio.addActionListener(e -> {
      occurrencesSpinner.setEnabled(false);
      endDateSpinner.setEnabled(true);
    });

    endDateSpinner.setEnabled(false);
  }

  /**
   * Builds the panel with basic event fields.
   */
  private void initFormPanel() {
    JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    form.add(new JLabel("Subject:"));
    form.add(subjectField);

    form.add(new JLabel("Location:"));
    form.add(locationField);

    form.add(new JLabel("Description:"));
    form.add(new JScrollPane(descriptionArea));

    form.add(new JLabel("Status:"));
    form.add(statusCombo);

    form.add(new JLabel("Start Time:"));
    form.add(startSpinner);

    form.add(new JLabel("End Time:"));
    form.add(endSpinner);

    add(form, BorderLayout.NORTH);
  }

  /**
   * Builds the recurrence options panel with weekday checkboxes
   * and termination options.
   */
  private void initRecurrencePanel() {
    JPanel recurrence = new JPanel(new BorderLayout(5, 5));
    recurrence.setBorder(BorderFactory.createTitledBorder("Recurrence"));

    // Weekday checkboxes
    JPanel weekdays = new JPanel(new GridLayout(2, 4, 5, 5));
    for (int i = 0; i < 7; i++) {
      weekdayBoxes[i] = new JCheckBox(weekdayNames[i]);
      weekdays.add(weekdayBoxes[i]);
    }

    // Termination condition
    JPanel endCondition = new JPanel(new GridLayout(2, 2, 5, 5));
    endCondition.add(occurrencesRadio);
    endCondition.add(occurrencesSpinner);
    endCondition.add(endDateRadio);
    endCondition.add(endDateSpinner);

    recurrence.add(weekdays, BorderLayout.NORTH);
    recurrence.add(endCondition, BorderLayout.CENTER);

    add(recurrence, BorderLayout.CENTER);
  }

  /**
   * Creates the action buttons (Create and Cancel).
   */
  private void initButtonPanel() {
    JButton create = new JButton("Create");
    JButton cancel = new JButton("Cancel");

    JPanel panel = new JPanel();

    create.addActionListener(e -> {
      if (validateInputs()) {
        confirmed = true;
        dispose();
      }
    });

    cancel.addActionListener(e -> dispose());

    panel.add(create);
    panel.add(cancel);

    add(panel, BorderLayout.SOUTH);
  }

  /**
   * Validates that subject is non-empty and at least one weekday is selected.
   *
   * @return true if validation passes
   */
  private boolean validateInputs() {
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Subject cannot be empty",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }

    boolean anyWeekdaySelected = false;
    for (JCheckBox box : weekdayBoxes) {
      if (box.isSelected()) {
        anyWeekdaySelected = true;
        break;
      }
    }

    if (!anyWeekdaySelected) {
      JOptionPane.showMessageDialog(this,
          "Please select at least one weekday",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }


  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSubject() {
    return subjectField.getText().trim();
  }

  public String getEventLocation() {
    return locationField.getText().trim();
  }

  public String getDescription() {
    return descriptionArea.getText().trim();
  }

  /**
   * Returns the start time for the first occurrence.
   *
   * @return start time with date and timezone
   */
  public ZonedDateTime getStart() {
    Date d = (Date) startSpinner.getValue();
    return ZonedDateTime.ofInstant(d.toInstant(),
        ZoneId.systemDefault()).with(date);
  }

  /**
   * Returns the end time for the first occurrence.
   *
   * @return end time with date and timezone
   */
  public ZonedDateTime getEnd() {
    Date d = (Date) endSpinner.getValue();
    return ZonedDateTime.ofInstant(d.toInstant(),
        ZoneId.systemDefault()).with(date);
  }

  public EventStatus getStatus() {
    return (EventStatus) statusCombo.getSelectedItem();
  }

  /**
   * Returns selected weekdays as a pattern string (e.g., "MTWRF").
   *
   * <p>Maps checkboxes to single-letter codes:</p>
   * <ul>
   *     <li>M = Monday</li>
   *     <li>T = Tuesday</li>
   *     <li>W = Wednesday</li>
   *     <li>R = Thursday</li>
   *     <li>F = Friday</li>
   *     <li>S = Saturday</li>
   *     <li>U = Sunday</li>
   * </ul>
   *
   * @return pattern string like "MWF" or "MTWRF"
   */
  public String getWeekdayPattern() {
    StringBuilder pattern = new StringBuilder();
    char[] codes = {'M', 'T', 'W', 'R', 'F', 'S', 'U'};

    for (int i = 0; i < 7; i++) {
      if (weekdayBoxes[i].isSelected()) {
        pattern.append(codes[i]);
      }
    }

    return pattern.toString();
  }

  /**
   * Returns whether the user chose to terminate by occurrence count.
   *
   * @return true if "Number of occurrences" radio is selected
   */
  public boolean useOccurrences() {
    return occurrencesRadio.isSelected();
  }

  /**
   * Returns the number of occurrences selected by the user.
   *
   * @return occurrence count (1-999)
   */
  public Integer getOccurrences() {
    return (Integer) occurrencesSpinner.getValue();
  }

  /**
   * Returns the end date selected by the user.
   *
   * @return end date for recurrence termination
   */
  public LocalDate getEndDate() {
    Date d = (Date) endDateSpinner.getValue();
    return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
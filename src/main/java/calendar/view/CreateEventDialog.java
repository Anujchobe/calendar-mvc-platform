package calendar.view;

import calendar.model.EventStatus;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/**
 * Modal dialog for collecting user input to create a single calendar event.
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Collect event details: subject, location, description, status</li>
 *     <li>Collect start and end times using time spinners</li>
 *     <li>Perform UI-level validation</li>
 *     <li>Provide getters for parent view to extract data</li>
 * </ul>
 */
public class CreateEventDialog extends JDialog {

  /**
   * Input field for event subject/title.
   */
  private final JTextField subjectField = new JTextField();

  /**
   * Input field for event location.
   */
  private final JTextField locationField = new JTextField();

  /**
   * Text area for event description.
   */
  private final JTextArea descriptionArea = new JTextArea(3, 20);

  /**
   * Dropdown for selecting event status (PUBLIC/PRIVATE).
   */
  private final JComboBox<EventStatus> statusCombo =
      new JComboBox<>(EventStatus.values());

  /**
   * Spinner for selecting start time (hours and minutes).
   */
  private final JSpinner startSpinner;

  /**
   * Spinner for selecting end time (hours and minutes).
   */
  private final JSpinner endSpinner;

  /**
   * Flag indicating user confirmation.
   */
  private boolean confirmed = false;

  /**
   * The date on which this event is being created.
   */
  private final LocalDate date;

  /**
   * Constructs a dialog for creating a new event on the specified date.
   *
   * <p>Time spinners default to current time but can be adjusted by user.</p>
   *
   * @param parent parent Swing frame
   * @param date   the date on which the event is being created
   */
  public CreateEventDialog(JFrame parent, LocalDate date) {
    super(parent, "Create Event", true);
    this.date = date;

    setSize(420, 360);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    // Initialize time spinners with current time as default
    startSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner = new JSpinner(new SpinnerDateModel());

    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));

    initFormPanel();
    initButtonPanel();
  }

  /**
   * Initializes the form panel with all input fields.
   *
   * <p>Uses a grid layout to organize labels and input components.</p>
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

    add(form, BorderLayout.CENTER);
  }

  /**
   * Initializes the button panel with Create and Cancel buttons.
   */
  private void initButtonPanel() {
    JButton ok = new JButton("Create");
    JButton cancel = new JButton("Cancel");

    JPanel panel = new JPanel();

    ok.addActionListener(e -> {
      if (validateInputs()) {
        confirmed = true;
        dispose();
      }
    });

    cancel.addActionListener(e -> dispose());

    panel.add(ok);
    panel.add(cancel);

    add(panel, BorderLayout.SOUTH);
  }

  /**
   * Validates user input before allowing dialog closure.
   *
   * <p>Currently only checks that subject is non-empty. Additional
   * validations (e.g., end time after start time) could be added.</p>
   *
   * @return true if validation passes, false otherwise
   */
  private boolean validateInputs() {
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(
          this,
          "Subject cannot be empty",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE
      );
      return false;
    }

    return true;
  }

  /**
   * Returns whether the user confirmed event creation.
   *
   * @return true if "Create" was clicked, false otherwise
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the event subject entered by the user.
   *
   * @return trimmed subject string
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Returns the event location entered by the user.
   *
   * @return trimmed location string
   */
  public String getEventLocation() {
    return locationField.getText().trim();
  }

  /**
   * Returns the event description entered by the user.
   *
   * @return trimmed description string
   */
  public String getDescription() {
    return descriptionArea.getText().trim();
  }

  /**
   * Returns the event start time as a ZonedDateTime.
   *
   * <p>Combines the dialog's date with the time selected in the spinner,
   * using the system default timezone.</p>
   *
   * @return start time with date and timezone
   */
  public ZonedDateTime getStart() {
    Date d = (Date) startSpinner.getValue();
    return ZonedDateTime.ofInstant(
        d.toInstant(),
        ZoneId.systemDefault()
    ).with(date);
  }

  /**
   * Returns the event end time as a ZonedDateTime.
   *
   * <p>Combines the dialog's date with the time selected in the spinner.</p>
   *
   * @return end time with date and timezone
   */
  public ZonedDateTime getEnd() {
    Date d = (Date) endSpinner.getValue();
    return ZonedDateTime.ofInstant(
        d.toInstant(),
        ZoneId.systemDefault()
    ).with(date);
  }

  /**
   * Returns the selected event status.
   *
   * @return PUBLIC or PRIVATE
   */
  public EventStatus getStatus() {
    return (EventStatus) statusCombo.getSelectedItem();
  }
}
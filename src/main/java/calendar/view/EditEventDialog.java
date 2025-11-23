package calendar.view;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
 * Modal dialog for editing existing calendar events.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Pre-populates fields with current event data</li>
 *     <li>Supports single event editing</li>
 *     <li>Supports batch editing (all events with same subject)</li>
 *     <li>Optional date filter for batch edits ("from this date onward")</li>
 * </ul>
 *
 * <p><b>Edit Modes:</b></p>
 * <ul>
 *     <li><b>Single:</b> Edit only the selected event instance</li>
 *     <li><b>Multiple (all):</b> Edit all events with the same subject</li>
 *     <li><b>Multiple (from date):</b> Edit events with same subject
 *         starting from a specific date</li>
 * </ul>
 *
 * <p><b>MVC Compliance:</b></p>
 * <ul>
 *     <li>Only collects user input - no model manipulation</li>
 *     <li>Parent view interprets edit mode and calls appropriate
 *         controller methods</li>
 *     <li>No business logic - pure data collection</li>
 * </ul>
 */
public class EditEventDialog extends AbstractViewDialog {

  private final JTextField subjectField = new JTextField();
  private final JTextField locationField = new JTextField();
  private final JTextArea descriptionArea = new JTextArea(3, 20);
  private final JComboBox<EventStatus> statusCombo =
      new JComboBox<>(EventStatus.values());

  private final JSpinner startSpinner;
  private final JSpinner endSpinner;

  /**
   * Checkbox to enable editing multiple events with same subject.
   */
  private final JCheckBox editMultipleCheckbox =
      new JCheckBox("Edit all events with this subject");

  /**
   * Checkbox to enable date filtering for batch edits.
   */
  private final JCheckBox fromDateCheckbox =
      new JCheckBox("Only from date:");

  /**
   * Date spinner for "from date" filtering.
   */
  private final JSpinner fromDateSpinner;

  private boolean confirmed = false;
  private final Event originalEvent;

  /**
   * Constructs an edit dialog pre-populated with existing event data.
   *
   * <p>All form fields are initialized with the event's current values.</p>
   *
   * @param parent parent frame
   * @param event  the event to edit (used for pre-population and identification)
   */
  public EditEventDialog(JFrame parent, Event event) {
    super(parent, "Edit Event");
    this.originalEvent = event;

    setSize(480, 500);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    // Initialize spinners
    startSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner = new JSpinner(new SpinnerDateModel());
    fromDateSpinner = new JSpinner(new SpinnerDateModel());

    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));
    fromDateSpinner.setEditor(
        new JSpinner.DateEditor(fromDateSpinner, "yyyy-MM-dd"));

    populateFieldsFromEvent(event);
    initFormPanel();
    initButtonPanel();

    // Disable from-date spinner initially
    fromDateSpinner.setEnabled(false);
    fromDateCheckbox.addActionListener(e ->
        fromDateSpinner.setEnabled(fromDateCheckbox.isSelected()));
  }

  /**
   * Pre-fills all form fields with the event's current values.
   *
   * <p>This ensures the user sees existing data and can modify only
   * the fields they wish to change.</p>
   *
   * @param event the event whose data should populate the form
   */
  private void populateFieldsFromEvent(Event event) {
    subjectField.setText(event.getSubject());
    locationField.setText(event.getLocation());
    descriptionArea.setText(event.getDescription());
    statusCombo.setSelectedItem(event.getStatus());

    startSpinner.setValue(Date.from(event.getStart().toInstant()));
    endSpinner.setValue(Date.from(event.getEnd().toInstant()));
    fromDateSpinner.setValue(Date.from(
        event.getStart().toLocalDate()
            .atStartOfDay(event.getStart().getZone()).toInstant()));
  }

  /**
   * Builds the form layout with all input fields and edit mode options.
   */
  private void initFormPanel() {
    JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
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

    form.add(new JLabel(""));
    form.add(editMultipleCheckbox);

    form.add(fromDateCheckbox);
    form.add(fromDateSpinner);

    add(form, BorderLayout.CENTER);
  }

  /**
   * Creates the Save and Cancel buttons.
   */
  private void initButtonPanel() {
    JButton save = new JButton("Save");
    JButton cancel = new JButton("Cancel");

    JPanel panel = new JPanel();

    save.addActionListener(e -> {
      if (validateInputs()) {
        confirmed = true;
        dispose();
      }
    });

    cancel.addActionListener(e -> dispose());

    panel.add(save);
    panel.add(cancel);

    add(panel, BorderLayout.SOUTH);
  }

  /**
   * Validates that the subject field is not empty.
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
    return true;
  }


  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the original event's unique identifier.
   *
   * <p>Used by the controller to identify which event to update.</p>
   *
   * @return event ID (subject + start + end as key)
   */
  public String getEventId() {
    return originalEvent.getSubject() + "|"
        +
        originalEvent.getStart() + "|"
        +
        originalEvent.getEnd();
  }

  /**
   * Returns the original event object for controller reference.
   *
   * @return the event being edited
   */
  public Event getOriginalEvent() {
    return originalEvent;
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
   * Returns the edited start time.
   *
   * <p>Preserves the original date and timezone, only updates the time.</p>
   *
   * @return updated start time
   */
  public ZonedDateTime getStart() {
    Date d = (Date) startSpinner.getValue();
    return ZonedDateTime.ofInstant(d.toInstant(),
            originalEvent.getStart().getZone())
        .with(originalEvent.getStart().toLocalDate());
  }

  /**
   * Returns the edited end time.
   *
   * <p>Preserves the original date and timezone, only updates the time.</p>
   *
   * @return updated end time
   */
  public ZonedDateTime getEnd() {
    Date d = (Date) endSpinner.getValue();
    return ZonedDateTime.ofInstant(d.toInstant(),
            originalEvent.getEnd().getZone())
        .with(originalEvent.getEnd().toLocalDate());
  }

  public EventStatus getStatus() {
    return (EventStatus) statusCombo.getSelectedItem();
  }

  /**
   * Returns whether the user wants to edit multiple events.
   *
   * @return true if "Edit all events with this subject" is checked
   */
  public boolean isEditMultiple() {
    return editMultipleCheckbox.isSelected();
  }

  /**
   * Returns whether the date filter is enabled for batch edits.
   *
   * @return true if "Only from date" checkbox is checked
   */
  public boolean isFromDateEnabled() {
    return fromDateCheckbox.isSelected();
  }

  /**
   * Returns the "from date" filter value.
   *
   * @return date from which to start batch editing
   */
  public LocalDate getFromDate() {
    Date d = (Date) fromDateSpinner.getValue();
    return d.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }
}
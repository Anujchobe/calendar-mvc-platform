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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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

/**
 * Modal dialog for editing existing calendar events.
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li>Pre-populates fields with current event data</li>
 *     <li>Three mutually exclusive edit modes using radio buttons</li>
 *     <li>Clear visual distinction between edit scopes</li>
 * </ul>
 *
 * <p><b>Edit Modes (Mutually Exclusive):</b></p>
 * <ul>
 *     <li><b>Single Event:</b> Edit only the selected event instance</li>
 *     <li><b>From This Onward:</b> Edit this event and all future occurrences</li>
 *     <li><b>Entire Series:</b> Edit all events with the same subject</li>
 * </ul>
 *
 * <p><b>MVC Compliance:</b></p>
 * <ul>
 *     <li>Only collects user input - no model manipulation</li>
 *     <li>Parent view interprets edit mode and calls appropriate controller methods</li>
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
   * Radio button for editing only this single event.
   */
  private final JRadioButton editSingleRadio =
      new JRadioButton("Edit only this event", true);

  /**
   * Radio button for editing from this event onward.
   */
  private final JRadioButton editFromThisOnwardRadio =
      new JRadioButton("Edit this event and all future occurrences");

  /**
   * Radio button for editing entire series.
   */
  private final JRadioButton editEntireSeriesRadio =
      new JRadioButton("Edit all events with this subject");

  private boolean confirmed = false;
  private final Event originalEvent;

  /**
   * Constructs an edit dialog pre-populated with existing event data.
   *
   * <p>All form fields are initialized with the event's current values.
   * Edit mode defaults to "single event" for safety.</p>
   *
   * @param parent parent frame
   * @param event  the event to edit (used for pre-population and identification)
   */
  public EditEventDialog(JFrame parent, Event event) {
    super(parent, "Edit Event");
    this.originalEvent = event;

    setSize(500, 550);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    startSpinner = new JSpinner(new SpinnerDateModel());
    endSpinner = new JSpinner(new SpinnerDateModel());

    startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
    endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));

    populateFieldsFromEvent(event);
    initFormPanel();
    initEditModePanel();
    initButtonPanel();
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
  }

  /**
   * Builds the form layout with all event property input fields.
   */
  private void initFormPanel() {
    JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
    form.setBorder(BorderFactory.createTitledBorder("Event Details"));

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
   * Creates the edit mode selection panel with mutually exclusive radio buttons.
   *
   * <p>Uses ButtonGroup to ensure only one edit mode can be selected at a time.
   * This prevents ambiguous edit operations and provides clear user intent.</p>
   */
  private void initEditModePanel() {
    JPanel modePanel = new JPanel(new GridLayout(3, 1, 5, 5));
    modePanel.setBorder(BorderFactory.createTitledBorder("Edit Scope"));

    ButtonGroup editModeGroup = new ButtonGroup();
    editModeGroup.add(editSingleRadio);
    editModeGroup.add(editFromThisOnwardRadio);
    editModeGroup.add(editEntireSeriesRadio);

    modePanel.add(editSingleRadio);
    modePanel.add(editFromThisOnwardRadio);
    modePanel.add(editEntireSeriesRadio);

    if (originalEvent.getSeriesId() == null || originalEvent.getSeriesId().isBlank()) {
      editFromThisOnwardRadio.setEnabled(false);
      editEntireSeriesRadio.setEnabled(false);
      editSingleRadio.setSelected(true);

      JLabel infoLabel = new JLabel("(This is not a recurring event)");
      infoLabel.setFont(infoLabel.getFont().deriveFont(10f));
      modePanel.add(infoLabel);
    }

    add(modePanel, BorderLayout.CENTER);
  }

  /**
   * Creates the Save and Cancel buttons.
   */
  private void initButtonPanel() {
    JButton save = new JButton("Save Changes");
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

  /**
   * Returns whether the user confirmed the edit.
   *
   * @return true if "Save Changes" was clicked
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Returns the original event object for controller reference.
   *
   * @return the event being edited
   */
  public Event getOriginalEvent() {
    return originalEvent;
  }

  /**
   * Returns the edited subject.
   *
   * @return trimmed subject string
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Returns the edited location.
   *
   * @return trimmed location string
   */
  public String getEventLocation() {
    return locationField.getText().trim();
  }

  /**
   * Returns the edited description.
   *
   * @return trimmed description string
   */
  public String getDescription() {
    return descriptionArea.getText().trim();
  }

  /**
   * Returns the edited start time.
   *
   * <p>Preserves the original date and timezone, only updates the time component.</p>
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
   * <p>Preserves the original date and timezone, only updates the time component.</p>
   *
   * @return updated end time
   */
  public ZonedDateTime getEnd() {
    Date d = (Date) endSpinner.getValue();
    return ZonedDateTime.ofInstant(d.toInstant(),
            originalEvent.getEnd().getZone())
        .with(originalEvent.getEnd().toLocalDate());
  }

  /**
   * Returns the selected event status.
   *
   * @return PUBLIC or PRIVATE
   */
  public EventStatus getStatus() {
    return (EventStatus) statusCombo.getSelectedItem();
  }

  /**
   * Returns whether user selected "Edit only this event" mode.
   *
   * @return true if single event edit mode is selected
   */
  public boolean isEditSingle() {
    return editSingleRadio.isSelected();
  }

  /**
   * Returns whether user selected "Edit from this onward" mode.
   *
   * @return true if from-this-onward mode is selected
   */
  public boolean isEditFromThisOnward() {
    return editFromThisOnwardRadio.isSelected();
  }

  /**
   * Returns whether user selected "Edit entire series" mode.
   *
   * @return true if entire series mode is selected
   */
  public boolean isEditEntireSeries() {
    return editEntireSeriesRadio.isSelected();
  }

  /**
   * Returns the edit mode as an enum for cleaner code in parent view.
   *
   * @return the selected edit mode
   */
  public EditMode getEditMode() {
    if (editSingleRadio.isSelected()) {
      return EditMode.SINGLE;
    } else if (editFromThisOnwardRadio.isSelected()) {
      return EditMode.FROM_THIS_ONWARD;
    } else if (editEntireSeriesRadio.isSelected()) {
      return EditMode.ENTIRE_SERIES;
    }
    return EditMode.SINGLE; // Default fallback
  }

  /**
   * Enum representing the three edit modes.
   * This makes code in SwingCalendarView cleaner.
   */
  public enum EditMode {
    SINGLE,
    FROM_THIS_ONWARD,
    ENTIRE_SERIES
  }
}
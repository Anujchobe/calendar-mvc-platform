package calendar.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * A modal dialog for creating a new calendar by collecting its
 * name and timezone from the user.
 *
 * <p>This dialog:
 * <ul>
 *     <li>Gathers user input only</li>
 *     <li>Does NOT communicate with the controller or model</li>
 *     <li>Offers getters so the parent view (e.g., {@link SwingCalendarView})
 *         can extract the inputs and then notify the controller listener</li>
 * </ul>
 */
public class CreateCalendarDialog extends JDialog {

  private final JTextField nameField = new JTextField();
  private final JTextField timezoneField = new JTextField("America/New_York");

  private boolean confirmed = false;

  /**
   * Constructs a modal dialog for creating a new calendar.
   *
   * @param parent parent frame from which this dialog is launched
   */
  public CreateCalendarDialog(JFrame parent) {
    super(parent, "Create New Calendar", true);

    setSize(360, 220);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    initFormPanel();
    initButtonPanel();
  }


  /**
   * Initializes the panel containing input fields.
   *
   * <p>Uses a 2x2 grid layout with labels on the left and input fields
   * on the right.</p>
   */
  private void initFormPanel() {
    JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    form.add(new JLabel("Calendar Name:"));
    form.add(nameField);

    form.add(new JLabel("Timezone:"));
    form.add(timezoneField);

    add(form, BorderLayout.CENTER);
  }

  /**
   * Creates the OK/Cancel buttons and attaches their action listeners.
   *
   * <p>OK button validates input before closing the dialog.
   * Cancel button simply closes without setting the confirmed flag.</p>
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
   * Performs minimal UI-level validation on user inputs.
   *
   * <p>Checks:</p>
   * <ul>
   *     <li>Calendar name is not empty</li>
   *     <li>Timezone is not empty</li>
   * </ul>
   *
   * <p>Displays error dialogs for invalid input.</p>
   *
   * @return true if inputs are valid, false otherwise
   */
  private boolean validateInputs() {
    if (nameField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(
          this,
          "Calendar name cannot be empty.",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE
      );
      return false;
    }

    if (timezoneField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(
          this,
          "Timezone cannot be empty.",
          "Validation Error",
          JOptionPane.ERROR_MESSAGE
      );
      return false;
    }

    return true;
  }

  /**
   * Indicates whether the user approved the dialog by clicking the
   * <em>Create</em> button.
   *
   * <p>This allows the calling view to distinguish between a successful
   * submission and a cancellation or window close action.</p>
   *
   * @return {@code true} if the user confirmed creation; {@code false} otherwise
   */
  public boolean isConfirmed() {
    return confirmed;
  }


  /**
   * Retrieves the calendar name entered by the user.
   *
   * <p>This method should be called only after checking that
   * {@link #isConfirmed()} returned {@code true}.</p>
   *
   * @return the trimmed calendar name text field value
   */
  public String getCalendarName() {
    return nameField.getText().trim();
  }


  /**
   * Retrieves the timezone entered by the user.
   *
   * <p>The returned value is expected to be an IANA timezone string
   * (e.g., {@code "America/New_York"}). Validation is handled only at
   * the UI level; the controller should perform full validation.</p>
   *
   * @return the trimmed timezone text field value
   */
  public String getTimezone() {
    return timezoneField.getText().trim();
  }

}

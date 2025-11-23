package calendar.view;

import calendar.controller.Ifeatures;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Base dialog class that standardizes common behavior.
 *
 * <p>All dialogs extend this class to inherit controller binding,
 * sizing, centering, and disposal logic.</p>
 */
public abstract class AbstractViewDialog extends JDialog implements IviewEventFirer {

  /** Reference to controller listener. */
  protected Ifeatures features;

  /**
   * Constructs a modal dialog with the specified owner and title.
   *
   * <p>The dialog is automatically configured to:</p>
   * <ul>
   *     <li>Be modal (blocks parent window)</li>
   *     <li>Dispose on close (releases resources)</li>
   *     <li>Be centered relative to parent</li>
   * </ul>
   *
   * @param owner parent frame that owns this dialog
   * @param title title displayed in the dialog's title bar
   */
  protected AbstractViewDialog(JFrame owner, String title) {
    super(owner, title, true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  /**
   * Sets the controller features interface.
   *
   * <p>Subclasses can use {@code this.features} to fire events to the
   * controller when user actions occur.</p>
   *
   * @param features the controller listener
   */
  @Override
  public void setFeatures(Ifeatures features) {
    this.features = features;
  }
}

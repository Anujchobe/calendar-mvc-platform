package calendar.view;

import calendar.controller.Ifeatures;

/**
 * High-level abstraction for the graphical calendar view.
 *
 * <p>This interface decouples the controller from any concrete Swing classes.
 * A controller interacts only through this API, never directly with JFrame
 * or specific Swing components.</p>
 */
public interface Icalendarview {

  /**
   * Makes the calendar window visible to the user.
   */
  void display();

  /**
   * Requests the view to refresh its visual state,
   * e.g., after model changes or navigation.
   */
  void refresh();

  /**
   * Displays a user-friendly error message to the user.
   *
   * @param message description of what went wrong
   */
  void showError(String message);

  /**
   * Displays a user-friendly success or informational message.
   *
   * @param message description of the successful operation
   */
  void showSuccess(String message);

  /**
   * Registers the controller's feature interface as the recipient of
   * all user actions.
   *
   * <p>The view does not know concrete controller types. It only
   * depends on this interface, which the real controller implements.</p>
   *
   * @param features the controller-facing features implementation
   */
  void setFeatures(Ifeatures features);
}

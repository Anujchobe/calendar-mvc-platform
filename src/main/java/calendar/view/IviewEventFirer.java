package calendar.view;

import calendar.controller.Ifeatures;

/**
 * Interface for view components that need to send user actions
 * to the controller.
 *
 * <p>This interface ensures that any UI component (dialog, panel, or window)
 * can receive a reference to the controller's {@link Ifeatures} implementation
 * and use it to fire user actions.</p>
 *
 */
public interface IviewEventFirer {

  /**
   * Sets the controller interface so this view component can fire
   * user actions back to the controller.
   *
   * @param features controller interface implementing all user-triggered
   *                 actions
   */
  void setFeatures(Ifeatures features);
}
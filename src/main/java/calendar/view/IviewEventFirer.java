package calendar.view;

import calendar.controller.Ifeatures;

/**
 * Mixin interface for all view components that need to dispatch events
 * to the controller.
 *
 * <p>This interface ensures that any UI component (dialog, panel, or window)
 * can receive a reference to the controller's {@link Ifeatures} implementation
 * and use it to fire user actions.</p>
 *
 * <p><b>Design Pattern:</b> Observer/Listener pattern where view components
 * fire events and the controller listens.</p>
 */
public interface IviewEventFirer {

  /**
   * Sets the controller interface so this view component can fire
   * user actions back to the controller.
   *
   * <p>Implementing classes should store this reference and use it
   * to delegate all business logic operations.</p>
   *
   * @param features controller interface implementing all user-triggered
   *                 actions
   */
  void setFeatures(Ifeatures features);
}
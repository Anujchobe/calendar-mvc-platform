package calendar.controller;

import calendar.model.IcalendarManager;

/**
 * Represents a generic command that can be executed in the Virtual Calendar application.
 *
 * <p>All commands operate through an {@link IcalendarManager}, which manages multiple
 * calendars and their events. Each command is responsible for validating its arguments
 * and performing its corresponding action.</p>
 */
public interface Command {

  /**
   * Executes this command on the given calendar manager.
   *
   * <p>Implementations should handle their own validation and
   * throw appropriate exceptions when encountering invalid arguments
   * or missing context (such as no active calendar).</p>
   *
   * @param manager the calendar manager to execute the command on
   * @throws IllegalArgumentException if the command arguments are invalid
   * @throws IllegalStateException if an event-level command is executed
   *                               without an active calendar context
   */
  void execute(IcalendarManager manager);
}

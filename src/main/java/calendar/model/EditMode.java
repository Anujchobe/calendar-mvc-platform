package calendar.model;

/**
 * Specifies the scope of editing for events that belong to a series.
 */
public enum EditMode {

  /** Edit only the specified instance. */
  SINGLE,

  /** Edit the specified instance and all following events in the series. */
  FROM_THIS_ONWARD,

  /** Edit all events in the series (before and after the specified one). */
  ENTIRE_SERIES
}

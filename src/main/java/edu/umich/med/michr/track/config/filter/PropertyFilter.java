package edu.umich.med.michr.track.config.filter;

import edu.umich.med.michr.track.config.PropertySource;

/**
 * Filters properties based on certain criteria.
 */
public interface PropertyFilter {
  /**
   * Filters properties based on the given criteria.
   *
   * @param source The property source to filter
   * @param filterCriteria The criteria to use for filtering
   * @return A new PropertySource containing the filtered properties
   */
  PropertySource filterProperties(PropertySource source, String filterCriteria);
}

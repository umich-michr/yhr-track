package edu.umich.med.michr.track.config.filter;

import edu.umich.med.michr.track.config.PropertySource;

public interface PropertyFilter {
  PropertySource filterProperties(PropertySource source, String filterCriteria);
}

package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;

public interface ParameterNamingStrategy {
  String resolve(StandardParameter parameter);
}

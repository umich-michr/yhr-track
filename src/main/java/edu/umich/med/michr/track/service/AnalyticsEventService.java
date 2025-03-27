package edu.umich.med.michr.track.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AnalyticsEventService {
  void processAndSaveEvent(HttpServletRequest request);
}

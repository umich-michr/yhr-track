package edu.umich.med.michr.track.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public interface OriginValidator {
  void validate(HttpServletRequest request);
}

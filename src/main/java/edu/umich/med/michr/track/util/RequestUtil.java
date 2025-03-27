package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RequestUtil {
  public String getParameterValue(StandardParameter parameter, HttpServletRequest request) {
    return request.getParameter(parameter.getName(request.getMethod()));
  }

  public Map<String, String> extractCustomAttributes(HttpServletRequest request) {
    final String requestMethod = request.getMethod().toUpperCase();

    // Create a map of single values from the request
    final Map<String, String> allParams = request.getParameterMap().entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue()[0]
        ));

    for (StandardParameter param : StandardParameter.values()) {
      allParams.remove(param.getName(requestMethod));
    }
    return allParams;
  }

  public String extractIpAddress(HttpServletRequest request) {
    return Stream.of("X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP")
        .map(request::getHeader)
        .filter(this::isValidIp)
        .findFirst()
        .orElseGet(request::getRemoteAddr);
  }

  private boolean isValidIp(String ip) {
    return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
  }
}


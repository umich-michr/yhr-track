package edu.umich.med.michr.track.domain;

import edu.umich.med.michr.track.util.CamelCaseNamingStrategy;
import edu.umich.med.michr.track.util.HyphenSeparatedNamingStrategy;

import java.util.HashMap;
import java.util.Map;

public enum StandardParameter {
  CLIENT_ID,USER_ID,EVENT_TYPE,PAGE;
  private final Map<String, String> requestMethodParamNameMap = new HashMap<>();
  StandardParameter() {
    final String camelCaseParamName  = new CamelCaseNamingStrategy().resolve(this);
    final String hyphenSeparatedParamName  = new HyphenSeparatedNamingStrategy().resolve(this);

    requestMethodParamNameMap.put("GET", hyphenSeparatedParamName);
    requestMethodParamNameMap.put("POST", camelCaseParamName);
  }
  public String getName(String httpMethod){
    return requestMethodParamNameMap.getOrDefault(httpMethod.toUpperCase(), "POST");
  }
}

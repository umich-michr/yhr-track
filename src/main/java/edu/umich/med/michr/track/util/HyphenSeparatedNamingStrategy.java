package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;

public class HyphenSeparatedNamingStrategy implements ParameterNamingStrategy {
  @Override
  public String resolve(StandardParameter parameter) {
    return toHyphenSeparated(parameter.name(), "_");
  }

  protected static String toHyphenSeparated(String characterSeparatedWords, String separator) {
    if(characterSeparatedWords == null){
      return null;
    }
    final String replaced = characterSeparatedWords.toLowerCase().replace(separator, "-");
    return replaced.replaceAll("^-+", "").replaceAll("-+$", "");
  }
}

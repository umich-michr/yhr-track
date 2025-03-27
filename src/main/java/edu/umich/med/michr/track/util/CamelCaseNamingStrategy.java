package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CamelCaseNamingStrategy implements ParameterNamingStrategy {
  @Override
  public String resolve(StandardParameter parameter) {
    return toCamelCase(parameter.name(),"_");
  }

  protected static String toCamelCase(String characterSeparatedWords, String separator) {
    if (characterSeparatedWords == null) return null;

    String[] words = characterSeparatedWords.split(separator);
    // Find the index of the first non-empty word.
    int firstIndex = 0;
    while (firstIndex < words.length && words[firstIndex].isEmpty()) {
      firstIndex++;
    }
    if (firstIndex >= words.length) {
      return "";
    }

    String firstWord = words[firstIndex].toLowerCase();
    String rest = Arrays.stream(words, firstIndex + 1, words.length)
        .filter(word -> !word.isEmpty())
        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
        .collect(Collectors.joining());
    return firstWord + rest;
  }
}

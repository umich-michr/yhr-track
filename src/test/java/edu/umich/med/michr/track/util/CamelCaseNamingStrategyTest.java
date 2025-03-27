package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static edu.umich.med.michr.track.domain.StandardParameter.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CamelCaseNamingStrategy Tests")
class CamelCaseNamingStrategyTest {

  @Nested
  @DisplayName("resolve Method Tests")
  class ResolveMethodTests {

    private static Stream<Arguments> resolveMethodTestCases() {
      return Stream.of(
          Arguments.of(CLIENT_ID, "clientId"),
          Arguments.of(USER_ID, "userId"),
          Arguments.of(EVENT_TYPE, "eventType"),
          Arguments.of(PAGE, "page")
      );
    }

    @ParameterizedTest
    @MethodSource("resolveMethodTestCases")
    @DisplayName("testResolve_variousInputFormats")
    void testResolve_variousInputFormats(StandardParameter parameter, String expectedCamelCase) {
      CamelCaseNamingStrategy strategy = new CamelCaseNamingStrategy();
      assertEquals(expectedCamelCase, strategy.resolve(parameter));
    }
  }

  @Nested
  @DisplayName("toCamelCase Private Method Tests")
  class ToCamelCaseMethodTests {

    private static Stream<Arguments> toCamelCaseTestCases() {
      return Stream.of(
          Arguments.of(null, null),
          Arguments.of("", ""),
          Arguments.of("client-id", "clientId"),
          Arguments.of("user-id", "userId"),
          Arguments.of("event-type", "eventType"),
          Arguments.of("page", "page"),
          Arguments.of("-leading-hyphen", "leadingHyphen"),
          Arguments.of("trailing-hyphen-", "trailingHyphen"),
          Arguments.of("multiple-hyphens-in-between", "multipleHyphensInBetween"),
          Arguments.of("-leading-trailing-MiXed-case-trailing-", "leadingTrailingMixedCaseTrailing")
      );
    }

    @ParameterizedTest
    @MethodSource("toCamelCaseTestCases")
    @DisplayName("testToCamelCase_variousWordArrays")
    void testToCamelCase_variousWordArrays(String word, String expectedCamelCase) {
      String actualCamelCase = CamelCaseNamingStrategy.toCamelCase(word, "-");
      assertEquals(expectedCamelCase, actualCamelCase);
    }
  }
}

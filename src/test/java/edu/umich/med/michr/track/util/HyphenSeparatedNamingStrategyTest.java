package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("HyphenSeparatedNamingStrategy Tests")
class HyphenSeparatedNamingStrategyTest {

  private final HyphenSeparatedNamingStrategy strategy = new HyphenSeparatedNamingStrategy();

  @Nested
  @DisplayName("toHyphenSeparated Tests")
  class ToHyphenSeparatedTests {

    @ParameterizedTest(name = "{0} gives {1} with separator *")
    @CsvSource({
        "*leading*mixED, leading-mixed",
        "tRailiNg*mixED*, trailing-mixed",
        "*both*leading*tRailiNg*mixED*, both-leading-trailing-mixed",
        "CLIENT*ID, client-id",
        "USER*ID, user-id",
        "EVENT*TYPE, event-type",
        "PAGE, page"
    })
    @DisplayName("testToHyphenSeparated_normalInput")
    void testToHyphenSeparate_normalInput(String input, String expected) {
      String actual = HyphenSeparatedNamingStrategy.toHyphenSeparated(input, "*");
      assertEquals(expected, actual);
    }

    @Test
    @DisplayName("should return empty string for empty string")
    void testToHyphenSeparate_emptyInput() {
      String actual = HyphenSeparatedNamingStrategy.toHyphenSeparated("", "_");
      assertEquals("", actual);
    }

    @Test
    @DisplayName("should return null for null input")
    void testToHyphenSeparate_nullInput() {
     assertNull(HyphenSeparatedNamingStrategy.toHyphenSeparated(null, "_"));
    }
  }

  @Nested
  @DisplayName("resolve Tests")
  class ResolveTests {

    @ParameterizedTest
    @CsvSource({
        "CLIENT_ID, client-id",
        "EVENT_TYPE, event-type",
        "USER_ID, user-id",
        "PAGE, page"
    })
    @DisplayName("returns {1} for {0}")
    void testResolve_returnsHyphenSeparatedValue_forEnum(String enumName, String expected) {
      String actual = strategy.resolve(StandardParameter.valueOf(enumName));

      assertEquals(expected, actual);
    }
  }
}

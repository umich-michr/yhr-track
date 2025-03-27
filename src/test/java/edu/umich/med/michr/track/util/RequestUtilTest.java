package edu.umich.med.michr.track.util;

import edu.umich.med.michr.track.domain.StandardParameter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@DisplayName("RequestUtil Unit Tests")
class RequestUtilTest {

  private final RequestUtil requestUtil = new RequestUtil();
  private final HttpServletRequest request = mock(HttpServletRequest.class);

  @Nested
  @DisplayName("getParameterValue Tests")
  class GetParameterValueTests {

    @ParameterizedTest(name = "should return null when {1} submitted with request param {0} is not present")
    @MethodSource("edu.umich.med.michr.track.util.RequestUtilTest$GetParameterValueTests#provideStandardParameters")
    @DisplayName("Should return null when parameter is missing")
    void testGetParameterValue_MissingParameterReturnsNull(StandardParameter parameter, String method) {
      when(request.getMethod()).thenReturn(method);

     final String actual = requestUtil.getParameterValue(parameter, request);

      assertNull(actual);
    }

    static Stream<Arguments> provideStandardParameters() {
      return Stream.of(
          Arguments.of(StandardParameter.CLIENT_ID, "GET"),
          Arguments.of(StandardParameter.USER_ID, "POST"),
          Arguments.of(StandardParameter.EVENT_TYPE, "POST"),
          Arguments.of(StandardParameter.PAGE, "GET")
      );
    }
  }

  @Nested
  @DisplayName("extractCustomAttributes Tests")
  class ExtractCustomAttributesTests {

    @Test
    @DisplayName("should return custom attributes excluding standard parameters")
    void testExtractCustomAttributes_RemovesStandardParamsAndKeepsOthers() {
      when(request.getMethod()).thenReturn("POST");
      when(request.getParameterMap()).thenReturn(Map.of(
          "clientId", new String[]{"123"},
          "userId", new String[]{"456"},
          "eventType", new String[]{"click"},
          "page", new String[]{"home"},
          "customKey", new String[]{"customValue"}
      ));

      final Map<String, String> actual = requestUtil.extractCustomAttributes(request);

      assertThat(actual).containsEntry("customKey", "customValue")
          .doesNotContainKeys("clientId", "userId", "eventType", "page");
    }
  }

  @Nested
  @DisplayName("extractIpAddress Tests")
  class ExtractIpAddressTests {

    @ParameterizedTest(name = "testExtractIpAddress_Header{0}PresentReturnsCorrectIp")
    @MethodSource("edu.umich.med.michr.track.util.RequestUtilTest$ExtractIpAddressTests#provideIpHeaders")
    @DisplayName("Should extract the first valid IP from headers")
    void testExtractIpAddress_HeaderPresentReturnsCorrectIp(String headerName, String expectedIp) {
      when(request.getHeader(headerName)).thenReturn(expectedIp);
      when(request.getRemoteAddr()).thenReturn("192.168.1.1");

      final String actual = requestUtil.extractIpAddress(request);

      verify(request, never()).getRemoteAddr();
      assertEquals(expectedIp, actual);
    }

    static Stream<Arguments> provideIpHeaders() {
      return Stream.of(
          Arguments.of("X-Forwarded-For", "203.0.113.1"),
          Arguments.of("Proxy-Client-IP", "198.51.100.2"),
          Arguments.of("WL-Proxy-Client-IP", "192.0.2.3")
      );
    }

    @Test
    @DisplayName("testExtractIpAddress_NoValidHeadersUsesRemoteAddr")
    void testExtractIpAddress_NoValidHeadersUsesRemoteAddr() {
      String expectedIp = "192.168.1.1";
      when(request.getHeader("X-Forwarded-For")).thenReturn("");
      when(request.getHeader("Proxy-Client-IP")).thenReturn("unKnown");
      when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
      when(request.getRemoteAddr()).thenReturn(expectedIp);

      final String actual = requestUtil.extractIpAddress(request);

      verify(request, times(1)).getRemoteAddr();
      assertEquals(expectedIp, actual);
    }
  }
}

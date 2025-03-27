package edu.umich.med.michr.track.controller;

import edu.umich.med.michr.track.service.AnalyticsEventService;
import edu.umich.med.michr.track.service.OriginValidator;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsEventController {

  private final OriginValidator originValidator;
  private final AnalyticsEventService analyticsEventService;

  @Inject
  public AnalyticsEventController(OriginValidator originValidator, AnalyticsEventService analyticsEventService) {
    this.originValidator = originValidator;
    this.analyticsEventService = analyticsEventService;
  }

  @PostMapping(value = "/events", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<Void> post(HttpServletRequest request) {

    originValidator.validate(request);

    analyticsEventService.processAndSaveEvent(request);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/events")
  public ResponseEntity<byte[]> get(HttpServletRequest request) {

    originValidator.validate(request);

    analyticsEventService.processAndSaveEvent(request);

    // Create headers with precise cache control
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_GIF);

    // Prevent caching
    headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
    headers.setPragma("no-cache");
    headers.setExpires(0);

    // Add a timestamp parameter to prevent caching
    headers.setETag("\"" + System.currentTimeMillis() + "\"");

    return ResponseEntity.ok().headers(headers).body(TRACKING_PIXEL);
  }

  private static final byte[] TRACKING_PIXEL = new byte[] {
      // GIF header for a 1x1 transparent pixel
      (byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38, (byte)0x39, (byte)0x61,
      (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x80, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0xff,
      (byte)0xff, (byte)0x21, (byte)0xf9, (byte)0x04, (byte)0x01, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x2c, (byte)0x00, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00,
      (byte)0x00, (byte)0x02, (byte)0x02, (byte)0x44, (byte)0x01, (byte)0x00,
      (byte)0x3b
  };
}

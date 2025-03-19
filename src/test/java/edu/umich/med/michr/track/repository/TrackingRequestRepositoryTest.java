package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.TrackingRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/tracking-request.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TrackingRequestRepositoryTest {

  @Autowired
  private TrackingRequestRepository trackingRequestRepository;

  @Test
  void testSaveAndFindById() {
    final TrackingRequest trackingRequest = TrackingRequest.builder("site1", "/home", LocalDateTime.now())
        .withUserId("user123")
        .withUserAgent("Mozilla/5.0")
        .withBrowserLanguage("en-US")
        .withCookieLanguage("en")
        .withIpAddress("192.168.0.1")
        .build();

    final TrackingRequest savedRequest = trackingRequestRepository.save(trackingRequest);
    final Optional<TrackingRequest> foundRequest = trackingRequestRepository.findById(savedRequest.getId());

    assertTrue(foundRequest.isPresent());
    assertEquals("site1", foundRequest.get().getSiteId());
    assertEquals("user123", foundRequest.get().getUserId());
  }

  @Test
  void testFindAll() {
    final TrackingRequest trackingRequest = TrackingRequest.builder("site1", "/page1", LocalDateTime.now())
        .build();

    trackingRequestRepository.save(trackingRequest);

    final TrackingRequest trackingRequest2 = TrackingRequest.builder("site2", "/page2", LocalDateTime.now())
        .build();
    trackingRequestRepository.save(trackingRequest2);

    final List<TrackingRequest> allRequests = trackingRequestRepository.findAll();

    assertEquals(3, allRequests.size());
  }

  @Test
  void testDelete() {
    final TrackingRequest trackingRequest = TrackingRequest.builder("site1", "/page1", LocalDateTime.now())
        .build();
    final TrackingRequest savedRequest = trackingRequestRepository.save(trackingRequest);

    trackingRequestRepository.deleteById(savedRequest.getId());

    assertFalse(trackingRequestRepository.findById(savedRequest.getId()).isPresent());
  }
}

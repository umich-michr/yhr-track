package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.AnalyticsEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/analytics-event.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AnalyticsEventRepositoryTest {

  @Autowired
  private AnalyticsEventRepository repository;

  @Test
  void testSaveAndFindById() {
    final AnalyticsEvent trackingRequest = AnalyticsEvent.builder("client1", "user123", "pageView", "/home", Instant.now())
        .userAgent("Mozilla/5.0")
        .browserLanguage("en-US")
        .customAttributes(Map.of("lang", "en"))
        .ipAddress("192.168.0.1")
        .build();

    final AnalyticsEvent savedRequest = repository.save(trackingRequest);
    final Optional<AnalyticsEvent> foundRequest = repository.findById(savedRequest.getId());

    assertTrue(foundRequest.isPresent());
    assertEquals("client1", foundRequest.get().getClientId());
    assertEquals("user123", foundRequest.get().getUserId());
  }

  @Test
  void testFindAll() {
    final AnalyticsEvent trackingRequest = AnalyticsEvent.builder("client1", "userId", "pageView", "/home", Instant.now())
        .build();

    repository.save(trackingRequest);

    final AnalyticsEvent trackingRequest2 = AnalyticsEvent.builder("client2", "userId2", "pageView", "/home2", Instant.now())
        .build();
    repository.save(trackingRequest2);

    final List<AnalyticsEvent> allRequests = StreamSupport.stream(repository.findAll().spliterator(), false).toList();

    assertEquals(3, allRequests.size());
  }

  @Test
  void testDelete() {
    final AnalyticsEvent trackingRequest = AnalyticsEvent.builder("client1", "userId", "pageView", "/home", Instant.now())
        .build();
    final AnalyticsEvent savedRequest = repository.save(trackingRequest);

    repository.deleteById(savedRequest.getId());

    assertFalse(repository.findById(savedRequest.getId()).isPresent());
  }
}

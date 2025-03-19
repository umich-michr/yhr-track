package edu.umich.med.michr.track.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TrackAppConfig {
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone(); // or Clock.systemUTC(), as appropriate
  }
}

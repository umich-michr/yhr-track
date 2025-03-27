package edu.umich.med.michr.track.repository;

import edu.umich.med.michr.track.domain.AnalyticsEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsEventRepository extends CrudRepository<AnalyticsEvent, Long> { }

package edu.umich.med.michr.track.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "ANALYTICS_EVENT")
public class AnalyticsEvent {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analyticsEventSeq")
  @SequenceGenerator(name = "analyticsEventSeq", sequenceName = "ANALYTICS_EVENT_SEQ", allocationSize = 1)
  private Long id;

  // used to verify the event originates from an allowed client.
  @Column(name = "CLIENT_ID", nullable = false)
  private String clientId;

  @Column(name = "USER_ID", nullable = false, length = 36)
  private String userId;

  @Column(name = "EVENT", nullable = false)
  private String eventType;

  @Column(name = "PAGE", nullable = false)
  private String page;

  @Column(name = "EVENT_TIME", nullable = false)
  private Instant eventTimestamp;

  // Optional fields
  @Column(name = "IP_ADDRESS", length = 45)
  private String ipAddress;

  @Column(name = "USER_AGENT", length = 512)
  private String userAgent;

  @Column(name = "BROWSER_LANGUAGE", length = 64)
  private String browserLanguage;

  @ElementCollection
  @CollectionTable(name = "ANALYTICS_EVENT_ATTRIBUTE", joinColumns = @JoinColumn(name = "ANALYTICS_EVENT_ID"))
  @MapKeyColumn(name = "ATTRIBUTE_NAME")
  @Column(name = "ATTRIBUTE_VALUE")
  private Map<String, String> customAttributes = new HashMap<>();

  // JPA requires a no-args constructor; set as protected to prevent direct use.
  protected AnalyticsEvent() {}

  // Private constructor invoked by the builder.
  private AnalyticsEvent(Builder builder) {
    this.clientId = builder.clientId;
    this.userId = builder.userId;
    this.eventType = builder.eventType;
    this.page = builder.page;
    this.eventTimestamp = builder.eventTimestamp;
    this.ipAddress = builder.ipAddress;
    this.userAgent = builder.userAgent;
    this.browserLanguage = builder.browserLanguage;
    this.customAttributes = builder.customAttributes;
  }

  public Long getId() {
    return id;
  }

  public String getClientId() {
    return clientId;
  }

  public String getUserId() {
    return userId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getPage() {
    return page;
  }

  public Instant getEventTimestamp() {
    return eventTimestamp;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getBrowserLanguage() {
    return browserLanguage;
  }

  public Map<String, String> getCustomAttributes() {
    return customAttributes;
  }

  public static Builder builder(String clientId, String userId, String eventType, String page, Instant eventTimestamp) {
    return new Builder(clientId, userId, eventType, page, eventTimestamp);
  }

  public static class Builder {
    private final String clientId;
    private final String userId;
    private final String eventType;
    private final String page;
    private final Instant eventTimestamp;

    private String ipAddress;
    private String userAgent;
    private String browserLanguage;
    private final Map<String, String> customAttributes = new HashMap<>();

    private Builder(String clientId, String userId, String eventType, String page, Instant eventTimestamp) {
      if (clientId == null || clientId.isEmpty()) {
        throw new IllegalArgumentException("clientId is mandatory");
      }
      if (userId == null || userId.isEmpty()) {
        throw new IllegalArgumentException("userId is mandatory");
      }
      if (eventType == null || eventType.isEmpty()) {
        throw new IllegalArgumentException("eventType is mandatory");
      }
      if (page == null || page.isEmpty()) {
        throw new IllegalArgumentException("page is mandatory");
      }
      if (eventTimestamp == null) {
        throw new IllegalArgumentException("eventTimestamp is mandatory");
      }
      this.clientId = clientId;
      this.userId = userId;
      this.eventType = eventType;
      this.page = page;
      this.eventTimestamp = eventTimestamp;
    }

    public Builder ipAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder userAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public Builder browserLanguage(String browserLanguage) {
      this.browserLanguage = browserLanguage;
      return this;
    }

    public Builder customAttributes(Map<String, String> attributes) {
      if (attributes != null) {
        this.customAttributes.putAll(attributes);
      }
      return this;
    }

    public AnalyticsEvent build() {
      return new AnalyticsEvent(this);
    }
  }
}

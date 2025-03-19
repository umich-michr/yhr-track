package edu.umich.med.michr.track.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TRACKING_REQUEST")
public class TrackingRequest {
  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trackingReqestSeq")
  @SequenceGenerator(name = "trackingReqestSeq", sequenceName = "TRACKING_REQUEST_SEQ", allocationSize = 1)
  private Long id;
  @Column(name = "USER_ID")
  private String userId;
  @Column(name = "USER_AGENT")
  private String userAgent;
  @Column(name = "PAGE_URL")
  private String pageUrl;
  @Column(name = "COOKIE_LANGUAGE")
  private String cookieLanguage;
  @Column(name = "BROWSER_LANGUAGE")
  private String browserLanguage;
  @Column(name = "SITE_ID")
  private String siteId;
  @Column(name = "IP_ADDRESS")
  private String ipAddress;
  @Column(name = "LOG_TIME")
  private LocalDateTime timestamp;

  // No-arg constructor for JPA
  public TrackingRequest() {}

  private TrackingRequest(String userId, String userAgent, String pageUrl, String cookieLanguage, String browserLanguage, String siteId, String ipAddress, LocalDateTime timestamp) {
    this.userId = userId;
    this.userAgent = userAgent;
    this.pageUrl = pageUrl;
    this.cookieLanguage = cookieLanguage;
    this.browserLanguage = browserLanguage;
    this.siteId = siteId;
    this.ipAddress = ipAddress;
    this.timestamp = timestamp;
  }

  public Long getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getPageUrl() {
    return pageUrl;
  }

  public String getCookieLanguage() {
    return cookieLanguage;
  }

  public String getBrowserLanguage() {
    return browserLanguage;
  }

  public String getSiteId() {
    return siteId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public static Builder builder(String siteId, String pageUrl, LocalDateTime timestamp) {
    return new BuilderImpl(siteId, pageUrl, timestamp);
  }

  public interface Builder {
    Builder withUserId(String userId);
    Builder withUserAgent(String userAgent);
    Builder withCookieLanguage(String cookieLanguage);
    Builder withBrowserLanguage(String browserLanguage);
    Builder withIpAddress(String ipAddress);
    TrackingRequest build();
  }

  private static class BuilderImpl implements Builder{
    private final String pageUrl;
    private final String siteId;
    private final LocalDateTime timestamp;
    private String userId;
    private String userAgent;
    private String cookieLanguage;
    private String browserLanguage;
    private String ipAddress;

    public BuilderImpl(String siteId, String pageUrl, LocalDateTime timestamp) {
      this.siteId = siteId;
      this.pageUrl = pageUrl;
      this.timestamp = timestamp;
    }

    public BuilderImpl withUserId(String userId) {
      this.userId = userId;
      return this;
    }

    public BuilderImpl withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }

    public BuilderImpl withCookieLanguage(String cookieLanguage) {
      this.cookieLanguage = cookieLanguage;
      return this;
    }

    public BuilderImpl withBrowserLanguage(String browserLanguage) {
      this.browserLanguage = browserLanguage;
      return this;
    }

    public BuilderImpl withIpAddress(String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public TrackingRequest build() {
      return new TrackingRequest(userId, userAgent, pageUrl, cookieLanguage, browserLanguage, siteId, ipAddress, timestamp);
    }
  }
}

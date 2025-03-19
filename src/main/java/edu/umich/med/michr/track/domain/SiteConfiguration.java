package edu.umich.med.michr.track.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "SITE")
public class SiteConfiguration {

  @Id
  @Column(name = "SITE_ID")
  private String siteId;
  @Column(name = "SITE_NAME")
  private String siteName;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "SITE_AUTHORIZED_DOMAIN",
      joinColumns = @JoinColumn(name = "SITE_ID"))
  @Column(name = "DOMAIN")
  private List<String> authorizedDomains;

  public String getSiteId() {
    return siteId;
  }

  public String getSiteName() {
    return siteName;
  }

  public List<String> getAuthorizedDomains() {
    return authorizedDomains;
  }
}

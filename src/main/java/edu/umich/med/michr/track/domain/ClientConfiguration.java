package edu.umich.med.michr.track.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "CLIENT")
public class ClientConfiguration {

  @Id
  @Column(name = "ID")
  private String id;
  @Column(name = "NAME")
  private String name;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "AUTHORIZED_CLIENT_ORIGIN",
      joinColumns = @JoinColumn(name = "CLIENT_ID"))
  @Column(name = "AUTHORIZED_ORIGIN")
  private List<String> authorizedOrigins;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getAuthorizedOrigins() {
    return authorizedOrigins;
  }
}

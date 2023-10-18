package com.google.cloud.spring.data.datastore.it.testdomains;

import com.google.cloud.spring.data.datastore.core.mapping.Descendants;
import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import java.util.List;
import org.springframework.data.annotation.Id;

@Entity(name = "company-test")
public class IsolateCompany {
  @Id private Long id;

  public List<String> leaders;

  public String name;

  public IsolateCompany(Long id, List<String> leaders) {
    this.id = id;
    this.leaders = leaders;
  }
}

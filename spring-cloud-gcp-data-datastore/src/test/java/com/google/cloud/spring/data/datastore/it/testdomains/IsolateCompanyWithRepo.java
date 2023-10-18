package com.google.cloud.spring.data.datastore.it.testdomains;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;
import java.util.List;
import org.springframework.data.annotation.Id;

@Entity(name = "company-test-w-repo")
public class IsolateCompanyWithRepo {
  @Id private Long id;

  public List<String> leaders;

  public String name;

  public IsolateCompanyWithRepo(Long id, List<String> leaders) {
    this.id = id;
    this.leaders = leaders;
  }
}

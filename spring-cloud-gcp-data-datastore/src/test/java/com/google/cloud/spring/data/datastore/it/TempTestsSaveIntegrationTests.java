package com.google.cloud.spring.data.datastore.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.google.cloud.datastore.Key;
import com.google.cloud.spring.data.datastore.core.DatastoreTemplate;
import com.google.cloud.spring.data.datastore.core.mapping.DatastoreMappingContext;
import com.google.cloud.spring.data.datastore.core.mapping.DatastorePersistentEntity;
import com.google.cloud.spring.data.datastore.core.mapping.DatastorePersistentProperty;
import com.google.cloud.spring.data.datastore.it.testdomains.Company;
import com.google.cloud.spring.data.datastore.it.testdomains.DogRepository;
import com.google.cloud.spring.data.datastore.it.testdomains.EmbeddedEntity;
import com.google.cloud.spring.data.datastore.it.testdomains.IsolateCompany;
import com.google.cloud.spring.data.datastore.it.testdomains.IsolateCompanyWithRepo;
import com.google.cloud.spring.data.datastore.it.testdomains.PetRepository;
import com.google.cloud.spring.data.datastore.it.testdomains.ProductRepository;
import com.google.cloud.spring.data.datastore.it.testdomains.TestEntity;
import com.google.cloud.spring.data.datastore.it.testdomains.TestEntity.Shape;
import com.google.cloud.spring.data.datastore.it.testdomains.TestEntityRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnabledIfSystemProperty(named = "it.datastore", matches = "true")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DatastoreIntegrationTestConfiguration.class})
@SpringBootTest
public class TempTestsSaveIntegrationTests {
  // @Autowired private TestEntityRepository testEntityRepository;

  @Autowired private DatastoreTemplate datastoreTemplate;

  @Autowired private DatastoreMappingContext mappingContext;
  // private final TestEntity testEntityA = new TestEntity(1L, "red", 1L, Shape.CIRCLE, null);
  //
  // private final TestEntity testEntityB = new TestEntity(2L, "blue", 2L, Shape.CIRCLE, null);
  //
  // private final TestEntity testEntityC =
  //     new TestEntity(3L, "red", 1L, Shape.CIRCLE, null, new EmbeddedEntity("c"));
  //
  // private final TestEntity testEntityD =
  //     new TestEntity(4L, "red", 1L, Shape.SQUARE, null, new EmbeddedEntity("d"));
  //
  // private final List<TestEntity> allTestEntities =
  //     Arrays.asList(this.testEntityA, this.testEntityB, this.testEntityC, this.testEntityD);

  @AfterEach
  void deleteAll() {
    this.datastoreTemplate.deleteAll(IsolateCompany.class);
    this.datastoreTemplate.deleteAll(IsolateCompanyWithRepo.class);
  }

  @Test
  void loadingOfThings() {
    DatastorePersistentEntity<?> entity = mappingContext.getDatastorePersistentEntity(
        IsolateCompanyWithRepo.class);
    DatastorePersistentEntity<?> company = mappingContext.getDatastorePersistentEntity(
        IsolateCompany.class);

    DatastorePersistentProperty entityId = entity.getIdProperty();
    assertThat(entityId.getName()).isEqualTo("id");
    DatastorePersistentProperty companyId = company.getIdProperty();
    assertThat(companyId.getName()).isEqualTo("id");
  }

  // @Test
  // void testSaveRepo() {
  //
  //   this.datastoreTemplate.saveAll(this.allTestEntities);
  //   await()
  //       .atMost(20, TimeUnit.SECONDS)
  //       .untilAsserted(() -> assertThat(this.datastoreTemplate.count(TestEntity.class)).isEqualTo(4));
  // }

  @Test
  void testSaveTemplate() {
    IsolateCompany company = new IsolateCompany(1L, Collections.emptyList());
    company.name = "name1";
    // this.datastoreTemplate.save(company);
    Key key = this.datastoreTemplate.getKey(company);
    key.getName();
    assertThat(key.getId()).isEqualTo(1L);
    assertThat(key.getKind()).isEqualTo("company-test");
    // Company company1 = this.datastoreTemplate.findById(1L, Company.class);
    // assertThat(company1.name).isEqualTo(company.name);
  }

  @Test
  void testSaveTemplateTestEntity() {
    IsolateCompanyWithRepo company = new IsolateCompanyWithRepo(5L, Collections.emptyList());
    // TestEntity testEntityE = new TestEntity(5L, "red", 1L, Shape.CIRCLE, null);
    // this.datastoreTemplate.save(testEntityE);
    // await()
    //     .atMost(20, TimeUnit.SECONDS)
    //     .untilAsserted(
    //         () -> assertThat(this.testEntityRepository.findById(5L)).contains(testEntityE));
    Key key = this.datastoreTemplate.getKey(company);
    assertThat(key.getKind()).isEqualTo("company-test-w-repo");
    assertThat(key.getId()).isEqualTo(5L);

  }
}

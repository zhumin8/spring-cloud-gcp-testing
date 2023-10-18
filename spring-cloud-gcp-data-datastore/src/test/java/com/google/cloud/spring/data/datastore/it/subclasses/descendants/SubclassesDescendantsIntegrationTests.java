/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spring.data.datastore.it.subclasses.descendants;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.cloud.spring.data.datastore.core.DatastoreTemplate;
import com.google.cloud.spring.data.datastore.it.AbstractDatastoreIntegrationTests;
import com.google.cloud.spring.data.datastore.it.DatastoreIntegrationTestConfiguration;
import com.google.cloud.spring.data.datastore.it.subclasses.descendants.testdomains.EntityA;
import com.google.cloud.spring.data.datastore.it.subclasses.descendants.testdomains.EntityB;
import com.google.cloud.spring.data.datastore.it.subclasses.descendants.testdomains.EntityC;
import com.google.cloud.spring.data.datastore.it.subclasses.descendants.testdomains.SubclassesDescendantsEntityArepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DisabledInNativeImage
@EnabledIfSystemProperty(named = "it.datastore", matches = "true")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DatastoreIntegrationTestConfiguration.class})
public class SubclassesDescendantsIntegrationTests extends AbstractDatastoreIntegrationTests {

  @Autowired SubclassesDescendantsEntityArepository entityArepository;

  @Autowired private DatastoreTemplate datastoreTemplate;

  @AfterEach
  void deleteAll() {
    datastoreTemplate.deleteAll(EntityA.class);
    datastoreTemplate.deleteAll(EntityB.class);
    datastoreTemplate.deleteAll(EntityC.class);
  }

  @Test
  void testEntityCcontainsReferenceToEntityB() {
    EntityB entityB1 = new EntityB();
    EntityC entityC1 = new EntityC();
    entityB1.addEntityC(entityC1);
    entityArepository.saveAll(Arrays.asList(entityB1, entityC1));
    EntityB fetchedB = (EntityB) entityArepository.findById(entityB1.getId()).get();
    List<EntityC> entitiesCofB = fetchedB.getEntitiesC();
    assertThat(entitiesCofB).hasSize(1);
  }
}

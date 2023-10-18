package com.google.cloud.spring.data.datastore.it.testdomains;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;

public interface IsolateCompanyWithRepoRepository extends
    DatastoreRepository<IsolateCompanyWithRepo, Long> {}

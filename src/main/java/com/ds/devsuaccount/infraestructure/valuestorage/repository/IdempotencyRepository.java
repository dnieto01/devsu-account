package com.ds.devsuaccount.infraestructure.valuestorage.repository;

import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdempotencyRepository extends MongoRepository<Idempotency, String> {
}

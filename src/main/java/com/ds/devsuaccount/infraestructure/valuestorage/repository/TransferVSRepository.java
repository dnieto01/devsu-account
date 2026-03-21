package com.ds.devsuaccount.infraestructure.valuestorage.repository;

import com.ds.devsuaccount.domain.entity.Transfer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransferVSRepository extends MongoRepository<Transfer, String> {

    Transfer findByOriginId(String originId);

}

package com.ds.devsuaccount.infraestructure.valuestorage;

import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;

import java.util.List;
import java.util.Optional;

public interface IValueStorageService {

    Optional<Transfer> findById(String id);

    Transfer findByOriginId(String originId);

    List<Transfer> findAll();

    Transfer saveTransfer(Transfer transfer);

    void saveIdempotency(String key, Object value);

    Optional<Idempotency> getIdempotency(String Key);
}

package com.ds.devsuaccount.infraestructure.valuestorage;

import com.ds.devsuaccount.domain.entity.Transfer;
import com.ds.devsuaccount.infraestructure.cache.CacheService;
import com.ds.devsuaccount.infraestructure.valuestorage.entity.Idempotency;
import com.ds.devsuaccount.infraestructure.valuestorage.repository.IdempotencyRepository;
import com.ds.devsuaccount.infraestructure.valuestorage.repository.TransferVSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ValueStorageService implements IValueStorageService {

    @Autowired
    private TransferVSRepository repositoryValueStorage;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private CacheService cacheService;

    @Override
    public Optional<Transfer> findById(String id) {
        return repositoryValueStorage.findById(id);
    }

    @Override
    public Transfer findByOriginId(String originId) {
        return repositoryValueStorage.findByOriginId(originId);
    }

    @Override
    public List<Transfer> findAll() {
        return repositoryValueStorage.findAll();
    }

    @Override
    public Transfer saveTransfer(Transfer transfer) {
        return repositoryValueStorage.save(transfer);
    }

    @Override
    public void saveIdempotency(String key, Object value) {
        Idempotency idempotency = Idempotency.builder().id(key).value(value).build();
        idempotencyRepository.save(idempotency);
        cacheService.saveIdempotencyCache(key, idempotency);
    }

    @Override
    public Optional<Idempotency> getIdempotency(String key) {
        Optional<Idempotency> response = Optional.ofNullable(cacheService.getFromIdempotencyCache(key));
        if (response.isPresent()) {
            return idempotencyRepository.findById(key);
        }
        return response;
    }

}

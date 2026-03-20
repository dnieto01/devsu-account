package com.ds.devsuaccount.infraestructure.database.repository;

import com.ds.devsuaccount.infraestructure.database.entity.TransactionDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionDbEntity, UUID> {

    TransactionDbEntity getById(UUID id);

}

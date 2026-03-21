package com.ds.devsuaccount.infraestructure.database.repository;

import com.ds.devsuaccount.infraestructure.database.entity.AccountDbEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountDbEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AccountDbEntity> findWithLockByAccountNumber(String accountNumber);
}

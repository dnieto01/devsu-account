package com.ds.devsuaccount.infraestructure.database.repository;

import com.ds.devsuuser.infraestructure.database.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, String> {

}

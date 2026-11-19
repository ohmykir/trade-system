package org.chistyakov.tradesystem.repository;

import jakarta.persistence.LockModeType;
import org.chistyakov.tradesystem.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Instrument i WHERE i.seccode = :seccode")
    Optional<Instrument> findByIdForUpdate(String seccode);
}

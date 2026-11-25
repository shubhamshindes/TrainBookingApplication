package com.example.IRCTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.IRCTC.entity.Train;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Train t where t.id = :id")
    Optional<Train> findByIdForUpdate(@Param("id") Long id);
}

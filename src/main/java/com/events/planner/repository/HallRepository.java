package com.events.planner.repository;

import com.events.planner.entity.Hall;
import com.events.planner.entity.HallType;
import com.events.planner.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author MAU
 */
@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    Page<Hall> findByType(HallType type, Pageable pageable);

    Page<Hall> findByNameContainingIgnoreCase(String type, Pageable pageable);

    Page<Hall> findByCapacityGreaterThanEqual(int capacity, Pageable pageable);
@Query("""
       SELECT h
       FROM Hall h
       WHERE h.capacity >= :minCapacity
         AND NOT EXISTS (
             SELECT r
             FROM Reservation r
             WHERE r.hall = h
               AND r.status = :status
               AND r.start < :end
               AND r.end > :start
         )
       """)
List<Hall> findAvailableHalls(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        @Param("status") ReservationStatus status,
        @Param("minCapacity") int minCapacity
);
}

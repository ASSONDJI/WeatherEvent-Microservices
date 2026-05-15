package com.reservation.repository;

import com.reservation.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    Page<Reservation> findByUserId(String userId, Pageable pageable);

    Page<Reservation> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    Page<Reservation> findByUserIdAndType(String userId, String type, Pageable pageable);

    Page<Reservation> findByUserIdAndStatusAndType(String userId, String status, String type, Pageable pageable);

    Page<Reservation> findByCityAndStatus(String city, String status, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE " +
           "(:city IS NULL OR r.city = :city) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:dateFrom IS NULL OR r.date >= :dateFrom) AND " +
           "(:dateTo IS NULL OR r.date <= :dateTo)")
    Page<Reservation> search(
        @Param("city") String city,
        @Param("type") String type,
        @Param("status") String status,
        @Param("userId") String userId,
        @Param("dateFrom") String dateFrom,
        @Param("dateTo") String dateTo,
        Pageable pageable);

    List<Reservation> findByUserId(String userId);

    long countByStatus(String status);

    long countByType(String type);

    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.status = 'CONFIRMED'")
    Double sumTotalRevenue();

    @Query("SELECT r.city, COUNT(r) FROM Reservation r GROUP BY r.city ORDER BY COUNT(r) DESC")
    List<Object[]> findTopCities();
}

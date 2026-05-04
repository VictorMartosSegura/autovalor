package com.autovalor.api.repository;

import com.autovalor.api.model.Listing;
import com.autovalor.api.model.ListingStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Query("select l from Listing l join fetch l.user order by l.createdAt desc")
    List<Listing> findAllWithUser();

    @Query("select l from Listing l join fetch l.user where l.status <> 'HIDDEN' order by l.createdAt desc")
    List<Listing> findAllPublicWithUser();

    @Query(
            value = """
                    select l from Listing l join fetch l.user
                    where (:query is null or lower(l.title) like lower(concat('%', :query, '%'))
                        or lower(l.description) like lower(concat('%', :query, '%'))
                        or lower(l.brand) like lower(concat('%', :query, '%'))
                        or lower(l.model) like lower(concat('%', :query, '%')))
                    and (:brand is null or lower(l.brand) = lower(:brand))
                    and (:model is null or lower(l.model) = lower(:model))
                    and (:minPrice is null or l.price >= :minPrice)
                    and (:maxPrice is null or l.price <= :maxPrice)
                    and (:minYear is null or l.year >= :minYear)
                    and (:maxYear is null or l.year <= :maxYear)
                    and (:minKm is null or l.km >= :minKm)
                    and (:maxKm is null or l.km <= :maxKm)
                    and (:fuelType is null or lower(l.fuelType) = lower(:fuelType))
                    and (:transmission is null or lower(l.transmission) = lower(:transmission))
                    and (:status is null or l.status = :status)
                    and l.status <> 'HIDDEN'
                    """,
            countQuery = """
                    select count(l) from Listing l
                    where (:query is null or lower(l.title) like lower(concat('%', :query, '%'))
                        or lower(l.description) like lower(concat('%', :query, '%'))
                        or lower(l.brand) like lower(concat('%', :query, '%'))
                        or lower(l.model) like lower(concat('%', :query, '%')))
                    and (:brand is null or lower(l.brand) = lower(:brand))
                    and (:model is null or lower(l.model) = lower(:model))
                    and (:minPrice is null or l.price >= :minPrice)
                    and (:maxPrice is null or l.price <= :maxPrice)
                    and (:minYear is null or l.year >= :minYear)
                    and (:maxYear is null or l.year <= :maxYear)
                    and (:minKm is null or l.km >= :minKm)
                    and (:maxKm is null or l.km <= :maxKm)
                    and (:fuelType is null or lower(l.fuelType) = lower(:fuelType))
                    and (:transmission is null or lower(l.transmission) = lower(:transmission))
                    and (:status is null or l.status = :status)
                    and l.status <> 'HIDDEN'
                    """
    )
    Page<Listing> searchPublic(
            @Param("query") String query,
            @Param("brand") String brand,
            @Param("model") String model,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("minKm") Integer minKm,
            @Param("maxKm") Integer maxKm,
            @Param("fuelType") String fuelType,
            @Param("transmission") String transmission,
            @Param("status") ListingStatus status,
            Pageable pageable
    );

    List<Listing> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Listing> findAllByStatusOrderByCreatedAtDesc(ListingStatus status);
}

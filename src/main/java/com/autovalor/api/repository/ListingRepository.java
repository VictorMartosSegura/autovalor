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
                        or lower(l.model) like lower(concat('%', :query, '%'))
                        or lower(l.location) like lower(concat('%', :query, '%'))
                        or lower(l.province) like lower(concat('%', :query, '%')))
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
                    and (:location is null or lower(l.location) = lower(:location))
                    and (:province is null or lower(l.province) = lower(:province))
                    and (:sellerType is null or lower(l.sellerType) = lower(:sellerType))
                    and (:bodyType is null or lower(l.bodyType) = lower(:bodyType))
                    and (:doors is null or l.doors = :doors)
                    and (:minPowerCv is null or l.powerCv >= :minPowerCv)
                    and (:maxPowerCv is null or l.powerCv <= :maxPowerCv)
                    and (:environmentalLabel is null or lower(l.environmentalLabel) = lower(:environmentalLabel))
                    and (:warranty is null or l.warranty = :warranty)
                    and (:financeable is null or l.financeable = :financeable)
                    and (:status is null or l.status = :status)
                    and l.status <> 'HIDDEN'
                    """,
            countQuery = """
                    select count(l) from Listing l
                    where (:query is null or lower(l.title) like lower(concat('%', :query, '%'))
                        or lower(l.description) like lower(concat('%', :query, '%'))
                        or lower(l.brand) like lower(concat('%', :query, '%'))
                        or lower(l.model) like lower(concat('%', :query, '%'))
                        or lower(l.location) like lower(concat('%', :query, '%'))
                        or lower(l.province) like lower(concat('%', :query, '%')))
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
                    and (:location is null or lower(l.location) = lower(:location))
                    and (:province is null or lower(l.province) = lower(:province))
                    and (:sellerType is null or lower(l.sellerType) = lower(:sellerType))
                    and (:bodyType is null or lower(l.bodyType) = lower(:bodyType))
                    and (:doors is null or l.doors = :doors)
                    and (:minPowerCv is null or l.powerCv >= :minPowerCv)
                    and (:maxPowerCv is null or l.powerCv <= :maxPowerCv)
                    and (:environmentalLabel is null or lower(l.environmentalLabel) = lower(:environmentalLabel))
                    and (:warranty is null or l.warranty = :warranty)
                    and (:financeable is null or l.financeable = :financeable)
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
            @Param("location") String location,
            @Param("province") String province,
            @Param("sellerType") String sellerType,
            @Param("bodyType") String bodyType,
            @Param("doors") Integer doors,
            @Param("minPowerCv") Integer minPowerCv,
            @Param("maxPowerCv") Integer maxPowerCv,
            @Param("environmentalLabel") String environmentalLabel,
            @Param("warranty") Boolean warranty,
            @Param("financeable") Boolean financeable,
            @Param("status") ListingStatus status,
            Pageable pageable
    );

    List<Listing> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Listing> findAllByStatusOrderByCreatedAtDesc(ListingStatus status);
}

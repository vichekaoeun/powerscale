package io.powerscale.voting.scale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ScaleRepository extends JpaRepository<Scale, UUID> {

    Optional<Scale> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
            select s from Scale s
            where lower(s.title) like lower(concat('%', :query, '%'))
               or lower(s.criterion) like lower(concat('%', :query, '%'))
               or lower(coalesce(s.description, '')) like lower(concat('%', :query, '%'))
            """)
    Page<Scale> search(@Param("query") String query, Pageable pageable);
}

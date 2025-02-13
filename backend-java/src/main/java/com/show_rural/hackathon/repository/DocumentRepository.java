package com.show_rural.hackathon.repository;

import com.show_rural.hackathon.controller.dto.DocumentFilters;
import com.show_rural.hackathon.domain.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
            SELECT d FROM Document d
            LEFT JOIN DocumentIdentifier di on di.id = d.identifier.id
            WHERE (:#{#filters.search} IS NULL OR
                  LOWER(d.cnpj) LIKE LOWER(CONCAT('%', :#{#filters.search}, '%')) OR
                  LOWER(di.name) LIKE LOWER(CONCAT('%', :#{#filters.search}, '%')) OR
                  LOWER(d.protocol) LIKE LOWER(CONCAT('%', :#{#filters.search}, '%')) OR
                  LOWER(d.number) LIKE LOWER(CONCAT('%', :#{#filters.search}, '%')))
            AND (:#{#filters.date} IS NULL OR
                 d.limitDate <= :#{#filters.date})
            """)
    Page<Document> list(Pageable page, @Param("filters") DocumentFilters filters);

    @Query("""
        SELECT d FROM Document d
        WHERE d.limitDate <= CURRENT_DATE + :daysToNotify
        AND d.limitDate >= CURRENT_DATE
        ORDER BY d.limitDate ASC
        """)
    List<Document> findNextExpirations(Integer daysToNotify);
}

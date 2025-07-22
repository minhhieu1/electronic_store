package com.altech.electronicstore.repository;

import com.altech.electronicstore.entity.DealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealTypeRepository extends JpaRepository<DealType, Long> {
}

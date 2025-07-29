package com.rajeswarandhandapani.dblocking.repository;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}

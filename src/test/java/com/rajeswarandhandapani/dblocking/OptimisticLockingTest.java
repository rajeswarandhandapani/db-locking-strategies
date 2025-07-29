package com.rajeswarandhandapani.dblocking;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import com.rajeswarandhandapani.dblocking.repository.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OptimisticLockingTest {
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final Logger logger = LoggerFactory.getLogger(OptimisticLockingTest.class);

    @Test
    void testOptimisticLockingBlocksConcurrentUpdate() throws Exception {
        // Persist an InventoryItem entity
        final InventoryItem item;
        var newItem = new InventoryItem();
        newItem.setName("Book");
        newItem.setQuantity(10);
        item = inventoryItemRepository.saveAndFlush(newItem);
        logger.info("Persisted inventory item: {}", item);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Future<Exception> tx1 = executor.submit(() -> {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("TX1");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            logger.info("TX1 started, trying to acquire and update inventory item: {}", item);
            try {
                InventoryItem i1 = inventoryItemRepository.findById(item.getId()).orElseThrow();
                logger.info("TX1 acquired inventory item: {}", i1);
                latch.countDown(); // Signal tx2 to start
                Thread.sleep(4000); // Hold the transaction for a while
                i1.setQuantity(i1.getQuantity() - 1);
                inventoryItemRepository.save(i1);
                transactionManager.commit(status);
                logger.info("TX1 committed update for inventory item: {}", i1);
                return null;
            } catch (Exception e) {
                logger.error("TX1 failed: ", e);
                return e;
            }
        });

        Future<Exception> tx2 = executor.submit(() -> {
            latch.await(); // Wait for tx1 to read and hold
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("TX2");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            logger.info("TX2 started, trying to acquire and update inventory item: {}", item);
            try {
                InventoryItem i2 = inventoryItemRepository.findById(item.getId()).orElseThrow();
                logger.info("TX2 acquired inventory item: {}", i2);
                i2.setQuantity(i2.getQuantity() - 1);
                inventoryItemRepository.save(i2);
                transactionManager.commit(status);
                logger.info("TX2 committed update for inventory item: {}", i2);
                return null;
            } catch (Exception e) {
                logger.error("TX2 failed: ", e);
                return e;
            }
        });

        Exception exception1 = tx1.get();
        Exception exception2 = tx2.get();
        executor.shutdown();

        // Ensure tx2 succeeded
        assertNull(exception2, "Second transaction should succeed, but failed with: " + exception2);
        // Ensure tx1 failed due to optimistic locking
        assertTrue(exception1 instanceof OptimisticLockingFailureException,
                "First transaction should fail due to optimistic locking. Got: " + exception1);
    }
}

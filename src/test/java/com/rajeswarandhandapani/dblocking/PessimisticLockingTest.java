
package com.rajeswarandhandapani.dblocking;

import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class PessimisticLockingTest {
    
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final Logger logger = LoggerFactory.getLogger(PessimisticLockingTest.class);

    @Test
    void testPessimisticLockingBlocksConcurrentUpdate() throws Exception {
        // Step 1: Persist a Ticket entity
        final Ticket ticket;
        Ticket temp = new Ticket();
        temp.setName("Concert");
        temp.setBooked(false);
        ticket = ticketRepository.saveAndFlush(temp);
        logger.info("Persisted ticket: {}", ticket);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Future<Exception> tx1 = executor.submit(() -> {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("TX1");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            logger.info("TX1 started, trying to acquire lock on ticket: ");
            try {
                Ticket t1 = ticketRepository.findByIdAndName(1L, "Concert");
                logger.info("TX1 acquired lock on ticket: {}", t1);
                t1.setBooked(true);
                latch.countDown(); // Signal tx2 to start
                Thread.sleep(10000); // Hold the lock for a while
                ticketRepository.save(t1);
                transactionManager.commit(status);
                return null;
            } catch (Exception e) {
                transactionManager.rollback(status);
                return e;
            }
        });

        Future<Exception> tx2 = executor.submit(() -> {
            latch.await(); // Wait for tx1 to acquire lock
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("TX2");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            logger.info("TX2 started, trying to acquire lock on ticket: ");
            try {
                Ticket t2 = ticketRepository.findByIdAndName(1L, "Concert");
                logger.info("TX2 acquired lock on ticket: {}", t2);
                transactionManager.commit(status);
                return null;
            } catch (Exception e) {
                transactionManager.rollback(status);
                return e;
            }
        });

        Exception exception1 = null;
        Exception exception2 = null;
        try {
            exception1 = tx1.get();
        } catch (Exception e) {
            exception1 = e;
        }
        try {
            exception2 = tx2.get(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException te) {
            exception2 = te;
        }
        executor.shutdown();

        // Ensure tx1 succeeded
        assertNull(exception1, "First transaction should succeed, but failed with: " + exception1);

        // Ensure tx2 failed due to pessimistic locking or timeout
        assertTrue(isLockOrTimeout(exception2), "Second transaction should fail due to pessimistic locking or timeout. Got: " + exception2);

    }

    private static boolean isLockOrTimeout(Exception ex) {
        return ex instanceof PessimisticLockingFailureException || ex instanceof TimeoutException;
    }
    }

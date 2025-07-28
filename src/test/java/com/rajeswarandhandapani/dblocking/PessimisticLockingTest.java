
package com.rajeswarandhandapani.dblocking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// ...existing imports...
import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PessimisticLockingTest {
    // No Testcontainers or DynamicPropertySource needed. Use application.yaml config.
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void testPessimisticLockingBlocksConcurrentUpdate() throws Exception {
        // Step 1: Persist a Ticket entity
        final Ticket ticket;
        Ticket temp = new Ticket();
        temp.setName("Concert");
        temp.setBooked(false);
        ticket = ticketRepository.save(temp);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Future<Exception> tx1 = executor.submit(() -> {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setName("TX1");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            try {
                Ticket t1 = ticketRepository.findByIdAndName(ticket.getId(), ticket.getName());
                t1.setBooked(true);
                latch.countDown(); // Signal tx2 to start
                Thread.sleep(2000); // Hold the lock for a while
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
            try {
                ticketRepository.findByIdAndName(ticket.getId(), ticket.getName());
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

        // At least one transaction must fail due to pessimistic locking or timeout
        boolean atLeastOneFailed = isLockOrTimeout(exception1) || isLockOrTimeout(exception2);
        assertTrue(atLeastOneFailed, "Expected at least one transaction to fail due to pessimistic locking or timeout. Got: tx1=" + exception1 + ", tx2=" + exception2);

    }

    private static boolean isLockOrTimeout(Exception ex) {
        return ex instanceof PessimisticLockingFailureException || ex instanceof TimeoutException;
    }
    }

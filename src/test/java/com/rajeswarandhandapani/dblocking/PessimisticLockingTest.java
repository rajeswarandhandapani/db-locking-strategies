
package com.rajeswarandhandapani.dblocking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PessimisticLockingTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.43")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }
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

        Exception exception1 = tx1.get();
        Exception exception2 = tx2.get();
        executor.shutdown();

        assertNull(exception1, "First transaction should succeed");
        assertNotNull(exception2, "Second transaction should be blocked or throw an exception");
        assertTrue(exception2 instanceof CannotAcquireLockException ||
                   (exception2.getCause() != null && exception2.getCause() instanceof CannotAcquireLockException),
                "Expected a lock exception, but got: " + exception2);
    }
}

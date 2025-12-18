package com.ecommerce.inventory.base;

import com.ecommerce.inventory.InventoryServiceApplication;
import com.ecommerce.inventory.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    classes = InventoryServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "server.servlet.context-path=",
        "spring.kafka.listener.auto-startup=false", // Disable Kafka listener auto-start in tests
        "spring.kafka.consumer.enable-auto-commit=false" // Disable auto-commit
    }
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092",
        "log.retention.hours=1",
        "log.segment.bytes=1073741824",
        "num.network.threads=3",
        "num.io.threads=8",
        "controlled.shutdown.enable=false", // Disable controlled shutdown for faster tests
        "auto.create.topics.enable=true"
    },
    topics = {},
    count = 1 // Single broker
)
@Import(TestConfig.class)
@Transactional
@Sql(scripts = "/db/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestPropertySource(properties = {"server.servlet.context-path="})
public abstract class BaseIntegrationTest {

    /**
     * PostgreSQL Testcontainer for integration tests.
     * 
     * TODO: Optimize Docker client initialization to reduce test startup delays
     * 
     * Issue: Testcontainers Docker client initialization can take 10-30+ seconds, especially:
     * - On first test run (detecting Docker environment)
     * - When Docker Desktop is not running or slow to start
     * - On Windows with NpipeSocketClientProviderStrategy
     * 
     * First test run may take longer due to:
     * 1. Docker client initialization (Testcontainers detecting Docker environment)
     *    - Loading DockerClientProviderStrategy (NpipeSocketClientProviderStrategy on Windows)
     *    - ImageNameSubstitutor initialization
     * 2. Downloading PostgreSQL image if not cached (~100MB download)
     * 3. Container startup time (~5-10 seconds)
     * 
     * Solutions to speed up:
     * 1. Ensure Docker Desktop is running before tests
     * 2. Pre-pull image: docker pull postgres:15-alpine
     * 3. Create ~/.testcontainers.properties:
     *    testcontainers.reuse.enable=true
     *    testcontainers.ryuk.container.image=testcontainers/ryuk:0.5.1
     * 4. Container reuse is already enabled (.withReuse(true)) to speed up subsequent runs
     * 5. Consider using Testcontainers Desktop for better performance
     */
    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = 
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("inventory_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true) // Enable container reuse to speed up subsequent test runs
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(java.time.Duration.ofSeconds(60));

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.listener.auto-startup", () -> "false"); // Disable Kafka listener auto-start
        registry.add("server.servlet.context-path", () -> "");
    }

    @BeforeEach
    void setUp() {
        // Common setup code for all tests
    }

    @AfterEach
    void tearDown() {
        // Common cleanup code for all tests
    }
}

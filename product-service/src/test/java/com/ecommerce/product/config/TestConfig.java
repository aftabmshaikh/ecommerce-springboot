package com.ecommerce.product.config;

import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@Import(ProductService.class) // Import the actual service to test
public class TestConfig {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @MockBean
    private ProductRepository productRepository;
    
    // This will override the main application's kafkaTemplate with a mock
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return kafkaTemplate;
    }
    
    // Configure MockMvc
    @Bean
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
    }
    
    // Configure test data
    @Bean
    public void setupTestData() {
        // Setup mock repository behavior here if needed
        when(productRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
    }
}

package com.ecommerce.inventory.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.db.DatabaseTableMetrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "management.metrics.enable.jvm", havingValue = "true", matchIfMissing = true)
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "inventory-service")
                .commonTags("region", System.getenv().getOrDefault("REGION", "unknown"))
                .commonTags("environment", System.getenv().getOrDefault("ENV", "local"));
    }

    // Remove the duplicate JVM metrics beans to avoid conflicts with Spring Boot's auto-configuration
    // These are already provided by Spring Boot's JvmMetricsAutoConfiguration
    
    @Bean
    @ConditionalOnMissingBean
    public DatabaseTableMetrics inventoryTableMetrics(DataSource dataSource) {
        return new DatabaseTableMetrics(
                dataSource,
                "inventory",
                "inventory_items",
                Tags.of("table", "inventory_items")
        );
    }
}

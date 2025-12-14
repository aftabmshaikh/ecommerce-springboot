package com.ecommerce.inventory.service.metrics;

import com.ecommerce.inventory.model.InventoryItem;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryMetricsService {

    private static final String METRIC_PREFIX = "inventory.";
    private static final String STOCK_LEVEL = METRIC_PREFIX + "stock.level";
    private static final String STOCK_ADJUSTMENT = METRIC_PREFIX + "stock.adjustment";
    private static final String STOCK_RESERVATION = METRIC_PREFIX + "stock.reservation";
    private static final String STOCK_RELEASE = METRIC_PREFIX + "stock.release";
    private static final String STOCK_CONSUMPTION = METRIC_PREFIX + "stock.consumption";
    private static final String LOW_STOCK_ALERT = METRIC_PREFIX + "alert.low_stock";
    private static final String OUT_OF_STOCK_ALERT = METRIC_PREFIX + "alert.out_of_stock";
    private static final String RESTOCK_EVENT = METRIC_PREFIX + "event.restock";
    private static final String INVENTORY_OPERATION_DURATION = METRIC_PREFIX + "operation.duration";

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> stockLevels = new ConcurrentHashMap<>();
    private Timer operationTimer;
    private Counter stockAdjustmentCounter;
    private Counter stockReservationCounter;
    private Counter stockReleaseCounter;
    private Counter stockConsumptionCounter;
    private Counter lowStockAlertCounter;
    private Counter outOfStockAlertCounter;
    private Counter restockEventCounter;

    @PostConstruct
    public void init() {
        operationTimer = Timer.builder(INVENTORY_OPERATION_DURATION)
                .description("Time taken for inventory operations")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);

        stockAdjustmentCounter = Counter.builder(STOCK_ADJUSTMENT)
                .description("Number of stock adjustments made")
                .register(meterRegistry);

        stockReservationCounter = Counter.builder(STOCK_RESERVATION)
                .description("Number of stock reservations made")
                .register(meterRegistry);

        stockReleaseCounter = Counter.builder(STOCK_RELEASE)
                .description("Number of stock releases made")
                .register(meterRegistry);

        stockConsumptionCounter = Counter.builder(STOCK_CONSUMPTION)
                .description("Number of stock consumptions made")
                .register(meterRegistry);

        lowStockAlertCounter = Counter.builder(LOW_STOCK_ALERT)
                .description("Number of low stock alerts triggered")
                .register(meterRegistry);

        outOfStockAlertCounter = Counter.builder(OUT_OF_STOCK_ALERT)
                .description("Number of out of stock alerts triggered")
                .register(meterRegistry);

        restockEventCounter = Counter.builder(RESTOCK_EVENT)
                .description("Number of restock events")
                .register(meterRegistry);
    }

    public void recordStockLevel(InventoryItem item) {
        String sku = item.getSkuCode();
        int level = item.getAvailableQuantity();
        
        // Update or create gauge for this SKU
        stockLevels.computeIfAbsent(sku, k -> 
            meterRegistry.gauge(STOCK_LEVEL, 
                Tags.of("sku", sku, "productId", item.getProductId().toString()),
                new AtomicInteger(level))
        ).set(level);

        // Check for low stock conditions
        if (level <= 0) {
            outOfStockAlert(sku);
        } else if (level <= item.getLowStockThreshold()) {
            lowStockAlert(sku, level);
        }
    }

    public void recordStockAdjustment(String sku, int adjustment) {
        stockAdjustmentCounter.increment();
        log.debug("Stock adjustment recorded for SKU: {}, adjustment: {}", sku, adjustment);
    }

    public <T> T recordOperation(String operation, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return supplier.get();
        } finally {
            sample.stop(Timer.builder(INVENTORY_OPERATION_DURATION)
                .tag("operation", operation)
                .register(meterRegistry));
        }
    }

    public void recordStockReservation(String sku, int quantity) {
        stockReservationCounter.increment(quantity);
        log.debug("Stock reservation recorded for SKU: {}, quantity: {}", sku, quantity);
    }

    public void recordStockRelease(String sku, int quantity) {
        stockReleaseCounter.increment(quantity);
        log.debug("Stock release recorded for SKU: {}, quantity: {}", sku, quantity);
    }

    public void recordStockConsumption(String sku, int quantity) {
        stockConsumptionCounter.increment(quantity);
        log.debug("Stock consumption recorded for SKU: {}, quantity: {}", sku, quantity);
    }

    public void lowStockAlert(String sku, int currentLevel) {
        lowStockAlertCounter.increment();
        log.warn("Low stock alert for SKU: {}, current level: {}", sku, currentLevel);
    }

    public void outOfStockAlert(String sku) {
        outOfStockAlertCounter.increment();
        log.error("Out of stock alert for SKU: {}", sku);
    }

    public void recordRestockEvent(String sku, int quantity) {
        restockEventCounter.increment();
        log.info("Restock event recorded for SKU: {}, quantity: {}", sku, quantity);
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operation) {
        if (sample != null) {
            sample.stop(Timer.builder(INVENTORY_OPERATION_DURATION)
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }
}

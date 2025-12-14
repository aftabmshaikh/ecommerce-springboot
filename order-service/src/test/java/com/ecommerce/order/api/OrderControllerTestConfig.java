package com.ecommerce.order.api;

import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
public class OrderControllerTestConfig implements WebMvcConfigurer {
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(PageRequest.of(0, 10));
        resolvers.add(resolver);
    }
    
    @Bean
    public OrderService orderService() {
        return mock(OrderService.class);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    @Bean
    public OrderController orderController(OrderService orderService) {
        return new OrderController(orderService);
    }
    
    @Bean
    public MockMvc mockMvc(WebApplicationContext context) {
        return MockMvcBuilders
            .webAppContextSetup(context)
            .defaultRequest(MockMvcRequestBuilders.get("/").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .build();
    }
}

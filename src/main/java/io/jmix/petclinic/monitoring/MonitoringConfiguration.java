package io.jmix.petclinic.monitoring;

import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.security.util.JmixHttpSecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Permit public access to prometheus and health monitoring endpoints.
 */
@Configuration
public class MonitoringConfiguration {

    @Bean
    @Order(JmixSecurityFilterChainOrder.CUSTOM)
    SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/actuator/prometheus", "/actuator/health")
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable());
        JmixHttpSecurityUtils.configureAnonymous(http);
        return http.build();
    }
}

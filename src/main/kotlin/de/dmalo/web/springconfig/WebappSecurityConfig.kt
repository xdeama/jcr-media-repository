package de.dmalo.web.springconfig

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain


@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
open class WebappSecurityConfig {

    @Bean
    open fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .cors(withDefaults())
            .csrf(withDefaults())
            .authorizeHttpRequests { authorizationConfigurer ->
                authorizationConfigurer
                    .requestMatchers("/api/**", "/actuator/**").permitAll()
                    .anyRequest().permitAll()
            }
            .build()
    }

}

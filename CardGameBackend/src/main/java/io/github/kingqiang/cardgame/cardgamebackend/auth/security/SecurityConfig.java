package io.github.kingqiang.cardgame.cardgamebackend.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security：玩家/管理员 JWT 双链、白名单与 CORS。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final PlayerJwtAuthenticationFilter playerJwtAuthenticationFilter;
    private final RestAuthenticationHandler restAuthenticationHandler;

    public SecurityConfig(AdminJwtAuthenticationFilter adminJwtAuthenticationFilter,
                          PlayerJwtAuthenticationFilter playerJwtAuthenticationFilter,
                          RestAuthenticationHandler restAuthenticationHandler) {
        this.adminJwtAuthenticationFilter = adminJwtAuthenticationFilter;
        this.playerJwtAuthenticationFilter = playerJwtAuthenticationFilter;
        this.restAuthenticationHandler = restAuthenticationHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationHandler)
                        .accessDeniedHandler(restAuthenticationHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/admin/v1/admin/auth/login",
                                "/api/admin/v1/admin/auth/refresh",
                                "/api/v1/auth/wechat/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/orders/*/pay-callback"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/v1/**").authenticated()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(playerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

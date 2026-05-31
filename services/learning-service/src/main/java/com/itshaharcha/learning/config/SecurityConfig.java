package com.itshaharcha.learning.config;

import tools.jackson.databind.json.JsonMapper;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.common.web.ErrorResponse;
import com.itshaharcha.learning.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Public reads (spec marks these security: []). Method-scoped so the colon-verb
    // POST actions on the same paths (e.g. courses/{id}:enroll) still require auth.
    private static final String[] PUBLIC_GET_PATHS = {
            "/api/v1/learning/tracks",
            "/api/v1/learning/courses",
            "/api/v1/learning/courses/*",
            "/api/v1/learning/lessons/*",
            "/api/v1/learning/roadmaps",
            "/api/v1/learning/roadmaps/*",
            "/api/v1/learning/tutorials",
            "/api/v1/learning/docs",
            "/api/v1/learning/typing/lessons"
    };

    private static final String[] PUBLIC_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> writeError(response,
                HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED.name(),
                "Authentication required", request.getRequestURI());
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeError(response,
                HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN.name(),
                "Insufficient permissions", request.getRequestURI());
    }

    private void writeError(HttpServletResponse response, int status, String code,
                            String message, String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = ErrorResponse.of(status, code, message, path);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

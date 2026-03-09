package com.neuroncrafters.auth_app.config;

import com.neuroncrafters.auth_app.dtos.ApiError;
import com.neuroncrafters.auth_app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter  jwtAuthenticationFilter;
//    @Bean
//    public UserDetailsService user() {
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//
//        UserDetails user1 =  userBuilder.username("user").password("password").roles("USER").build();
//        UserDetails user2 =  userBuilder.username("admin").password("admin").roles("ADMIN").build();
//
//        return new InMemoryUserDetailsManager(user1, user2);
//
//    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // Setup CSRF rule
        // As our backend and frontend are separate we can disable CSRF
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequests ->
                // Skip authorization for register and login
                authorizeHttpRequests
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
        )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(
                                (request,
                                 response,
                                 authException) ->
                                {
                                    authException.printStackTrace();
                                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                    response.setContentType("application/json");
                                    String error = (String) request.getAttribute("error");
                                    String message = authException.getMessage();
                                    if (error != null) {
                                        message = error;
                                    }
                                    ApiError apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access !!", message, request.getRequestURI());
                                    var objectMapper = new ObjectMapper();
                                    response.getWriter().write(objectMapper.writeValueAsString(apiError));
                                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

}

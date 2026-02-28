package com.neuroncrafters.auth_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService user() {
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();

        UserDetails user1 =  userBuilder.username("user").password("password").roles("USER").build();
        UserDetails user2 =  userBuilder.username("admin").password("admin").roles("ADMIN").build();

        return new InMemoryUserDetailsManager(user1, user2);

    }

}

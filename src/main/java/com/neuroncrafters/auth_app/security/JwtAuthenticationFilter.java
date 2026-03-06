package com.neuroncrafters.auth_app.security;

import com.neuroncrafters.auth_app.helpers.UserHelper;
import com.neuroncrafters.auth_app.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Token will come in request header named "Authorization" and it's value starts with Bearer
        String header = request.getHeader("Authorization");

        logger.info("Authorization header received: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            // 1. token extract
            // 2. Validate token
            // 3. Create authentication
            // 4. Set authenticated inside security context

            // 1. extract token (starting with 7 so it removes "Bearer "
            String token = header.substring(7);
            try{
                // Check 1: for access token
                // so if the request is for refreshToken then return from here
                if (!jwtService.isAccessToken(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 2. Parse and Verify token
                Jws<Claims> parse = jwtService.parse(token);
                // get the payload
                Claims payload = parse.getPayload();

                // get userId
                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUUID(userId);

                // Load the user from the database using the userId from token claims.
                userRepository.findById(userUuid).ifPresent(user -> {
                    // Check 2: is user enabled
                    if (user.isEnable()) {
                        // Fetch users roles
                        List<GrantedAuthority> grantedAuthorities = user.getRoles() == null
                                ? Collections.emptyList()
                                : user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());

                        // 3. Create authentication
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                null,
                                grantedAuthorities);

                        // Attach request metadata (client IP, session ID, etc.) to the Authentication object
                        // so downstream security logic, handlers, and audit logs can access it via authentication.getDetails().
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Check 3: Make sure that SecurityContextHolder is null
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            // 5. Set authenticated inside security context
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                });
            } catch (ExpiredJwtException e) {
                e.printStackTrace();
            } catch (MalformedJwtException e) {
                e.printStackTrace();
            } catch (JwtException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        filterChain.doFilter(request, response);
    }
}

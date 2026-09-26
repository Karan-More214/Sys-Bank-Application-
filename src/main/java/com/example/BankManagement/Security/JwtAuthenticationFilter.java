package com.example.BankManagement.Security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads Authorization: Bearer <token> on every request, validates it, and - only if
 * valid - populates the SecurityContext with the token's subject (email) and role
 * claim. Never trusts anything the client sends outside the signed token itself.
 * Requests with no/invalid/expired token simply proceed unauthenticated; it's
 * SecurityConfig's authorizeHttpRequests rules (and @PreAuthorize) that then reject
 * them with 401/403 as appropriate.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = jwtService.validateAndParse(token);

            if (claims != null) {
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                String authority = "ROLE_" + (role == null ? "USER" : role.toUpperCase());

                var authentication = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

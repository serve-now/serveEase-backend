package com.servease.demo.config;

import com.servease.demo.service.UserService;
import com.servease.demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    private static final List<String> WHITELIST_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/signup",
            "/user/login",
            "/user/signup",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/"
    );


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 1) CORS Preflight(OPTIONS) 는 무조건 패스
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 2) 화이트리스트 경로는 패스 (prefix 매칭임)
        String path = request.getServletPath();
        for (String allow : WHITELIST_PREFIXES) {
            if (path.equals(allow) || path.startsWith(allow)) {
                return true;
            }
        }
        return false;
    }




    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header: {}", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("Authorization header is missing or invalid.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isTokenValid(token)) {
            String loginId = jwtUtil.getLoginIdFromToken(token);

            UserDetails userDetails = userService.loadUserByUsername(loginId);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("User '{}' authenticated successfully.", loginId);
        } else {
            log.error("Token is not valid.");
        }

        filterChain.doFilter(request, response);
    }


}
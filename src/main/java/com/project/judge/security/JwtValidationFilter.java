package com.project.judge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.judge.dto.response.ExceptionResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String,String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blisted_token:";
    private static final String USER_BLACKLIST_PREFIX = "blisted_user:";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken =  extractToken(request);

        try {
            String userId = jwtService.getIdFromToken(jwtToken);
            String role  = jwtService.getRoleFromToken(jwtToken);

            if(redisTemplate.hasKey(USER_BLACKLIST_PREFIX+userId)){
                sendExceptionResponse(response, HttpStatus.UNAUTHORIZED,
                        "DELETED_USER", "This user is deleted");
                return;
            }

            if(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX+jwtToken)){
                sendExceptionResponse(response, HttpStatus.UNAUTHORIZED,
                        "BLACKLISTED_TOKEN", "Given token is black-listed.");
                return;
            }


            if(jwtService.validateToken(jwtToken)){
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            }

            filterChain.doFilter(request, response);

        }catch (ExpiredJwtException e) {
            sendExceptionResponse(response, HttpStatus.UNAUTHORIZED,
                    "TOKEN_EXPIRED", "JWT token has expired");

        } catch (JwtException e) {
            sendExceptionResponse(response, HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN", "Invalid JWT token");

        }
        catch(Exception e){
            sendExceptionResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    "AUTHENTICATION_ERROR",
                    "Authentication failed"
            );
        }

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/v1/login") ||
                path.startsWith("/api/v1/judge/");
    }


    public void sendExceptionResponse(HttpServletResponse response,
                                      HttpStatus status,
                                      String code,
                                      String message) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ExceptionResponse errorResponse = new ExceptionResponse(status.value(), code, message, LocalDateTime.now());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }


    private String extractToken(HttpServletRequest request) {
        String bearerToken =  request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}

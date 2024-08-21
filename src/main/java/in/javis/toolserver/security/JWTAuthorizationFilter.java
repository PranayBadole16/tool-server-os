package in.javis.toolserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static in.javis.toolserver.security.SecurityConstants.HEADER_STRING;
import static in.javis.toolserver.security.SecurityConstants.TOKEN_PREFIX;


public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Skip authentication for non-existent endpoints
        if (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) != null) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        try {
            String token = request.getHeader(HEADER_STRING);

            if (token != null) {
                Map<String, Object> claim = JWTUtil.validateToken(token.replace(TOKEN_PREFIX, ""));
                return new UsernamePasswordAuthenticationToken(claim.get("userId"), null, new ArrayList<>());
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Exception While authenticating Request :", e);
            return null;
        }
    }
}

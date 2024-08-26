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

/**
 * Filter for JWT authorization.
 * <p>
 * This filter intercepts incoming requests to authenticate users based on JWT tokens.
 * It checks for the presence of a token in the request header, validates it, and sets the authentication in
 * the security context if the token is valid.
 * </p>
 */

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthorizationFilter.class);

    /**
     * Processes the HTTP request to perform JWT authentication.
     * <p>
     * This method is called once per request and performs the following actions:
     * 1. Skips authentication for error responses (e.g., 404 or 500).
     * 2. Retrieves the authentication token from the request header.
     * 3. Validates the token and sets the authentication context if the token is valid.
     * 4. Continues with the filter chain.
     * </p>
     *
     * @param request  the HttpServletRequest containing the incoming request data
     * @param response the HttpServletResponse used to send the response
     * @param chain    the FilterChain to continue the request-response flow
     * @throws IOException      if an I/O error occurs during request processing
     * @throws ServletException if a servlet-specific error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Skip authentication for error responses
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

    /**
     * Retrieves and validates the authentication token from the request.
     * <p>
     * This method extracts the JWT token from the request header, validates it, and creates an
     * {@link UsernamePasswordAuthenticationToken} if the token is valid. The token's claims are used to
     * set the authentication details.
     * </p>
     *
     * @param request the HttpServletRequest containing the token in the header
     * @return an authenticated UsernamePasswordAuthenticationToken if the token is valid, otherwise null
     */
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

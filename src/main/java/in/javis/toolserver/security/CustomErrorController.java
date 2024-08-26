package in.javis.toolserver.security;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Custom error controller for handling application errors.
 * <p>
 * This controller implements the {@link ErrorController} interface to provide a custom error response format
 * when errors occur within the application. It captures details such as status code, error message, and timestamp
 * and returns them in a structured {@link CustomErrorResponse} object.
 * </p>
 */
@RestController
public class CustomErrorController implements ErrorController {

    /**
     * Handles errors and constructs a custom error response.
     * <p>
     * This method is invoked when an error occurs in the application. It retrieves the error status code and message
     * from the request attributes and constructs a {@link CustomErrorResponse} with these details. The response also
     * includes a timestamp of when the error occurred.
     * </p>
     *
     * @param request the HttpServletRequest containing error details
     * @return a ResponseEntity containing the custom error response and HTTP status
     */
    @RequestMapping("/error")
    public ResponseEntity<CustomErrorResponse> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        HttpStatus status = HttpStatus.valueOf(statusCode);

        CustomErrorResponse errorResponse = new CustomErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(status.value());
        errorResponse.setError(status.getReasonPhrase());
        errorResponse.setMessage(errorMessage != null ? errorMessage : "No message available");

        return new ResponseEntity<>(errorResponse, status);
    }
}

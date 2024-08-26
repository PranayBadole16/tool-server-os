package in.javis.toolserver.security;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a custom error response structure.
 * <p>
 * This class encapsulates the details of an error response to be returned by the custom error controller.
 * It includes the timestamp of the error occurrence, the HTTP status code, a description of the error, and an
 * optional message providing additional details.
 * </p>
 */
@Data
public class CustomErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
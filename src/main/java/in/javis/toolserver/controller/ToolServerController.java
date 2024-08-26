package in.javis.toolserver.controller;

import in.javis.toolserver.pojo.EmbedS3FileRequest;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.service.ToolServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling requests related to the Tool Server.
 * <p>
 * This controller exposes endpoints for health checks, executing tool server requests,
 * and embedding Python scripts from S3.
 * </p>
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class ToolServerController {

    @Autowired
    private ToolServerService toolServerService;

    /**
     * Endpoint for health check.
     * <p>
     * This endpoint can be used to verify that the service is up and running.
     * </p>
     */
    @GetMapping(path = "/health")
    public void healthCheckAws() {
        // Health check endpoint - no implementation needed for basic health check.
    }

    /**
     * Endpoint for processing tool server requests.
     * <p>
     * This endpoint receives a {@link ToolServerRequest} object, logs the request details,
     * executes the request through the {@link ToolServerService}, and returns the result.
     * </p>
     *
     * @param toolServerRequest the request payload containing details for processing
     * @return a {@link ResponseEntity} containing the result of the request execution or an error status
     */
    @PostMapping("/tool-server")
    public ResponseEntity<Object> callToolServer(@RequestBody ToolServerRequest toolServerRequest) {
        try {
            log.warn("Tool Server Request - {}", toolServerRequest.toString());

            Object result = toolServerService.executeRequest(toolServerRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Exception while executing ToolServerRequest - {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint for embedding Python scripts from S3.
     * <p>
     * This endpoint receives an {@link EmbedS3FileRequest} object, uses the {@link ToolServerService}
     * to embed Python files, and returns an appropriate HTTP status.
     * </p>
     *
     * @param request the request payload containing details for embedding Python scripts
     * @return a {@link ResponseEntity>} indicating the outcome of the embedding operation
     */
    @PostMapping("/embed-python-script")
    public ResponseEntity<Object> callToolServer(@RequestBody EmbedS3FileRequest request) {
        try {
            toolServerService.embedPythonFiles(request);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception while embedding Python scripts - {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

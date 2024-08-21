package in.javis.toolserver.controller;

import in.javis.toolserver.pojo.EmbedS3FileRequest;
import in.javis.toolserver.pojo.ToolServerRequest;
import in.javis.toolserver.service.ToolServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class ToolServerController {

    @Autowired
    ToolServerService toolServerService;

    @GetMapping(path = "/health")
    public void healthCheckAws() {
    }

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

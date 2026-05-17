package com.notification.controller;

import com.notification.api.HealthApi;
import com.notification.dto.generated.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;

@RestController
public class HealthController implements HealthApi {

    @Override
    public ResponseEntity<HealthResponse> healthCheck() {
        HealthResponse response = new HealthResponse();
        response.setStatus(HealthResponse.StatusEnum.UP);
        response.setService("notification-service");
        response.setVersion("1.0.0");
        response.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.ok(response);
    }
}

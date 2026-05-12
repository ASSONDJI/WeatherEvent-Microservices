package com.event.mapper;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.OffsetDateTime;

@Component
public class EventMapper {
    
    public Map<String, Object> toSuccessResponse(List<Map<String, Object>> events) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", events.size());
        response.put("data", events);
        response.put("timestamp", OffsetDateTime.now().toString());
        return response;
    }
    
    public Map<String, Object> toErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", error);
        response.put("timestamp", OffsetDateTime.now().toString());
        return response;
    }
}

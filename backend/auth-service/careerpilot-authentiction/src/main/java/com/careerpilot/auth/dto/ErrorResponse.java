package com.careerpilot.auth.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
    OffsetDateTime timestampDateTime,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors
) {
    
}

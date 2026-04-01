package com.iotplatform.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAlertRequest {

    @Size(max = 1000, message = "Resolution note must not exceed 1000 characters")
    private String note;
}
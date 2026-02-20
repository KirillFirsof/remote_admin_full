package com.example.remote.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Запрос на обновление состояния агента")
public class HeartbeatRequest {
    @Schema(description = "ID агента", example = "1")
    private Long agentId;
    
    @Schema(description = "Загрузка CPU в процентах", example = "45.2")
    private Double cpuLoad;
    
    @Schema(description = "Свободная RAM в MB", example = "2048")
    private Integer freeRamMb;
}
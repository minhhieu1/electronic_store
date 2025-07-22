package com.altech.electronicstore.dto.deal;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealTypeResponseDto {
    private Long id;
    private String name;
    private String description;
    private String strategyClass;
}

package com.sgivu.vehicle.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MotorcycleResponse extends VehicleResponse {
  private String motorcycleType;
}

package com.sgivu.vehicle.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CarResponse extends VehicleResponse {
  private String bodyType;
  private String fuelType;
  private Integer numberOfDoors;
}

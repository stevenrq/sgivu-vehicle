package com.sgivu.vehicle.dto;

import com.sgivu.vehicle.enums.VehicleStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VehicleResponse {
  private Long id;
  private String brand;
  private String model;
  private Integer capacity;
  private String line;
  private String plate;
  private String motorNumber;
  private String serialNumber;
  private String chassisNumber;
  private String color;
  private String cityRegistered;
  private Integer year;
  private Integer mileage;
  private String transmission;
  private VehicleStatus status;
  private Double purchasePrice;
  private Double salePrice;
}

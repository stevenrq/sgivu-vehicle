package com.sgivu.vehicle.dto;

import com.sgivu.vehicle.enums.VehicleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MotorcycleSearchCriteria {
  private final String plate;
  private final String brand;
  private final String line;
  private final String model;
  private final String motorcycleType;
  private final String transmission;
  private final String cityRegistered;
  private final VehicleStatus status;
  private final Integer minYear;
  private final Integer maxYear;
  private final Integer minCapacity;
  private final Integer maxCapacity;
  private final Integer minMileage;
  private final Integer maxMileage;
  private final Double minSalePrice;
  private final Double maxSalePrice;
}

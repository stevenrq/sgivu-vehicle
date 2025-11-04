package com.sgivu.vehicle.mapper;

import com.sgivu.vehicle.dto.CarResponse;
import com.sgivu.vehicle.dto.MotorcycleResponse;
import com.sgivu.vehicle.entity.Car;
import com.sgivu.vehicle.entity.Motorcycle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "brand", target = "brand")
  @Mapping(source = "model", target = "model")
  @Mapping(source = "capacity", target = "capacity")
  @Mapping(source = "line", target = "line")
  @Mapping(source = "plate", target = "plate")
  @Mapping(source = "motorNumber", target = "motorNumber")
  @Mapping(source = "serialNumber", target = "serialNumber")
  @Mapping(source = "chassisNumber", target = "chassisNumber")
  @Mapping(source = "color", target = "color")
  @Mapping(source = "cityRegistered", target = "cityRegistered")
  @Mapping(source = "year", target = "year")
  @Mapping(source = "mileage", target = "mileage")
  @Mapping(source = "transmission", target = "transmission")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "purchasePrice", target = "purchasePrice")
  @Mapping(source = "salePrice", target = "salePrice")
  @Mapping(source = "available", target = "isAvailable")
  @Mapping(source = "bodyType", target = "bodyType")
  @Mapping(source = "fuelType", target = "fuelType")
  @Mapping(source = "numberOfDoors", target = "numberOfDoors")
  CarResponse toCarResponse(Car car);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "brand", target = "brand")
  @Mapping(source = "model", target = "model")
  @Mapping(source = "capacity", target = "capacity")
  @Mapping(source = "line", target = "line")
  @Mapping(source = "plate", target = "plate")
  @Mapping(source = "motorNumber", target = "motorNumber")
  @Mapping(source = "serialNumber", target = "serialNumber")
  @Mapping(source = "chassisNumber", target = "chassisNumber")
  @Mapping(source = "color", target = "color")
  @Mapping(source = "cityRegistered", target = "cityRegistered")
  @Mapping(source = "year", target = "year")
  @Mapping(source = "mileage", target = "mileage")
  @Mapping(source = "transmission", target = "transmission")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "purchasePrice", target = "purchasePrice")
  @Mapping(source = "salePrice", target = "salePrice")
  @Mapping(source = "available", target = "isAvailable")
  @Mapping(source = "motorcycleType", target = "motorcycleType")
  MotorcycleResponse toMotorcycleResponse(Motorcycle motorcycle);
}

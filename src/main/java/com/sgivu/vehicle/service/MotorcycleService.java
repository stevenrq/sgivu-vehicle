package com.sgivu.vehicle.service;

import com.sgivu.vehicle.entity.Motorcycle;
import java.util.List;
import java.util.Optional;

public interface MotorcycleService extends VehicleService<Motorcycle> {
  Optional<Motorcycle> findByMotorcycleType(String motorcycleType);

  List<Motorcycle> findByMotorcycleTypeContainingIgnoreCase(String motorcycleType);
}

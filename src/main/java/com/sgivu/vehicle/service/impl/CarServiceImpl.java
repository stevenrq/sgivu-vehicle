package com.sgivu.vehicle.service.impl;

import com.sgivu.vehicle.entity.Car;
import com.sgivu.vehicle.repository.CarRepository;
import com.sgivu.vehicle.service.CarService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarServiceImpl extends AbstractVehicleServiceImpl<Car, CarRepository>
    implements CarService {

  private final CarRepository carRepository;

  public CarServiceImpl(CarRepository carRepository) {
    super(carRepository);
    this.carRepository = carRepository;
  }

  @Override
  public Optional<Car> findByFuelType(String fuelType) {
    return carRepository.findByFuelType(fuelType);
  }

  @Override
  public Optional<Car> findByBodyType(String bodyType) {
    return carRepository.findByBodyType(bodyType);
  }

  @Override
  public List<Car> findByFuelTypeContainingIgnoreCase(String fuelType) {
    return carRepository.findByFuelTypeContainingIgnoreCase(fuelType);
  }

  @Override
  public List<Car> findByBodyTypeContainingIgnoreCase(String bodyType) {
    return carRepository.findByBodyTypeContainingIgnoreCase(bodyType);
  }

  @Transactional
  @Override
  public Optional<Car> update(Long id, Car vehicle) {
    return carRepository
        .findById(id)
        .map(
            existing -> {
              existing.setBodyType(vehicle.getBodyType());
              existing.setFuelType(vehicle.getFuelType());
              existing.setNumberOfDoors(vehicle.getNumberOfDoors());
              return carRepository.save(existing);
            });
  }
}

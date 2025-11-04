package com.sgivu.vehicle.controller;

import com.sgivu.vehicle.dto.CarResponse;
import com.sgivu.vehicle.entity.Car;
import com.sgivu.vehicle.enums.VehicleStatus;
import com.sgivu.vehicle.mapper.VehicleMapper;
import com.sgivu.vehicle.service.CarService;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/cars")
public class CarController {

  private final CarService carService;
  private final VehicleMapper vehicleMapper;

  public CarController(CarService carService, VehicleMapper vehicleMapper) {
    this.carService = carService;
    this.vehicleMapper = vehicleMapper;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('car:create')")
  public ResponseEntity<CarResponse> create(@RequestBody Car car, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().build();
    }
    CarResponse carResponse = vehicleMapper.toCarResponse(carService.save(car));
    return ResponseEntity.status(HttpStatus.CREATED).body(carResponse);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('car:read')")
  public ResponseEntity<CarResponse> getById(@PathVariable Long id) {
    return carService
        .findById(id)
        .map(car -> ResponseEntity.ok(vehicleMapper.toCarResponse(car)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAuthority('car:read')")
  public ResponseEntity<List<CarResponse>> getAll() {
    List<CarResponse> carResponses =
        carService.findAll().stream().map(vehicleMapper::toCarResponse).toList();
    return ResponseEntity.ok(carResponses);
  }

  @GetMapping("/page/{page}")
  @PreAuthorize("hasAuthority('car:read')")
  public ResponseEntity<Page<CarResponse>> getAllPaginated(@PathVariable Integer page) {
    return ResponseEntity.ok(
        carService.findAll(PageRequest.of(page, 10)).map(vehicleMapper::toCarResponse));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('car:update')")
  public ResponseEntity<CarResponse> update(
      @PathVariable Long id, @RequestBody Car car, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().build();
    }
    return carService
        .update(id, car)
        .map(updatedCar -> ResponseEntity.ok(vehicleMapper.toCarResponse(updatedCar)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('car:delete')")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    Optional<Car> carOptional = carService.findById(id);

    if (carOptional.isPresent()) {
      carService.deleteById(id);
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAuthority('car:update')")
  public ResponseEntity<Map<String, String>> changeStatus(
      @PathVariable Long id, @RequestBody VehicleStatus status) {
    if (carService.changeStatus(id, status).isPresent()) {
      return ResponseEntity.ok(Collections.singletonMap("status", status.name()));
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/count")
  @PreAuthorize("hasAuthority('car:read')")
  public ResponseEntity<Map<String, Long>> getCarCounts() {
    long totalCars = carService.findAll().size();
    long availableCars = carService.countByStatus(VehicleStatus.AVAILABLE);
    long unavailableCars = totalCars - availableCars;

    Map<String, Long> counts = new HashMap<>(Map.of("totalCars", totalCars));
    counts.put("availableCars", availableCars);
    counts.put("unavailableCars", unavailableCars);

    return ResponseEntity.ok(counts);
  }

  @GetMapping("/search")
  @PreAuthorize("hasAuthority('car:read')")
  public ResponseEntity<List<CarResponse>> searchCars(
      @RequestParam(required = false) String plate,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String line,
      @RequestParam(required = false) String model,
      @RequestParam(required = false) String fuelType,
      @RequestParam(required = false) String bodyType) {

    Set<Car> results = new LinkedHashSet<>();

    Map<String, String> filters = new HashMap<>();
    filters.put("plate", plate);
    filters.put("brand", brand);
    filters.put("line", line);
    filters.put("model", model);
    filters.put("fuelType", fuelType);
    filters.put("bodyType", bodyType);

    filters.forEach(
        (key, value) -> {
          if (StringUtils.hasText(value)) {
            switch (key) {
              case "plate" -> carService.findByPlate(value).ifPresent(results::add);
              case "brand" -> results.addAll(carService.findByBrandContainingIgnoreCase(value));
              case "line" -> results.addAll(carService.findByLineContainingIgnoreCase(value));
              case "model" -> results.addAll(carService.findByModelContainingIgnoreCase(value));
              case "fuelType" ->
                  results.addAll(carService.findByFuelTypeContainingIgnoreCase(value));
              case "bodyType" ->
                  results.addAll(carService.findByBodyTypeContainingIgnoreCase(value));
              default -> throw new IllegalArgumentException("Unknown filter: " + key);
            }
          }
        });

    List<CarResponse> carResponses = results.stream().map(vehicleMapper::toCarResponse).toList();
    return ResponseEntity.ok(carResponses);
  }
}

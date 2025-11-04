package com.sgivu.vehicle.controller;

import com.sgivu.vehicle.dto.MotorcycleResponse;
import com.sgivu.vehicle.entity.Motorcycle;
import com.sgivu.vehicle.enums.VehicleStatus;
import com.sgivu.vehicle.mapper.VehicleMapper;
import com.sgivu.vehicle.service.MotorcycleService;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/v1/motorcycles")
public class MotorcycleController {

  private final MotorcycleService motorcycleService;
  private final VehicleMapper vehicleMapper;

  public MotorcycleController(MotorcycleService motorcycleService, VehicleMapper vehicleMapper) {
    this.motorcycleService = motorcycleService;
    this.vehicleMapper = vehicleMapper;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('motorcycle:create')")
  public ResponseEntity<MotorcycleResponse> create(
      @RequestBody Motorcycle motorcycle, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().build();
    }
    Motorcycle savedMotorcycle = motorcycleService.save(motorcycle);
    MotorcycleResponse motorcycleResponse = vehicleMapper.toMotorcycleResponse(savedMotorcycle);
    return ResponseEntity.status(HttpStatus.CREATED).body(motorcycleResponse);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('motorcycle:read')")
  public ResponseEntity<MotorcycleResponse> getById(@PathVariable Long id) {
    return motorcycleService
        .findById(id)
        .map(vehicleMapper::toMotorcycleResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  @PreAuthorize("hasAuthority('motorcycle:read')")
  public ResponseEntity<List<MotorcycleResponse>> getAll() {
    return ResponseEntity.ok(
        motorcycleService.findAll().stream().map(vehicleMapper::toMotorcycleResponse).toList());
  }

  @GetMapping("/page/{page}")
  @PreAuthorize("hasAuthority('motorcycle:read')")
  public ResponseEntity<Page<MotorcycleResponse>> getAllPaginated(@PathVariable Integer page) {
    return ResponseEntity.ok(
        motorcycleService
            .findAll(PageRequest.of(page, 10))
            .map(vehicleMapper::toMotorcycleResponse));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('motorcycle:update')")
  public ResponseEntity<MotorcycleResponse> update(
      @PathVariable Long id, @RequestBody Motorcycle motorcycle, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().build();
    }
    return motorcycleService
        .update(id, motorcycle)
        .map(
            motorcycleUpdated ->
                ResponseEntity.ok(vehicleMapper.toMotorcycleResponse(motorcycleUpdated)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('motorcycle:delete')")
  public ResponseEntity<Void> deleteById(@PathVariable Long id) {
    if (motorcycleService.findById(id).isPresent()) {
      motorcycleService.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAuthority('motorcycle:update')")
  public ResponseEntity<Map<String, String>> changeStatus(
      @PathVariable Long id, @RequestBody VehicleStatus status) {
    if (motorcycleService.changeStatus(id, status).isPresent()) {
      return ResponseEntity.ok(Collections.singletonMap("status", status.name()));
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/count")
  @PreAuthorize("hasAuthority('motorcycle:read')")
  public ResponseEntity<Map<String, Long>> getMotorcycleCounts() {
    long totalMotorcycles = motorcycleService.findAll().size();
    long availableMotorcycles = motorcycleService.countByStatus(VehicleStatus.AVAILABLE);
    long unavailableMotorcycles = totalMotorcycles - availableMotorcycles;

    Map<String, Long> counts = new HashMap<>(Map.of("totalMotorcycles", totalMotorcycles));
    counts.put("availableMotorcycles", availableMotorcycles);
    counts.put("unavailableMotorcycles", unavailableMotorcycles);

    return ResponseEntity.ok(counts);
  }

  @GetMapping("/search")
  @PreAuthorize("hasAuthority('motorcycle:read')")
  public ResponseEntity<List<MotorcycleResponse>> searchMotorcycles(
      @RequestParam(required = false) String plate,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) String line,
      @RequestParam(required = false) String model,
      @RequestParam(required = false) String motorcycleType) {

    Set<Motorcycle> results = new LinkedHashSet<>();

    Map<String, String> filters =
        Map.of(
            "plate", plate,
            "brand", brand,
            "line", line,
            "model", model,
            "motorcycleType", motorcycleType);

    filters.forEach(
        (key, value) -> {
          if (StringUtils.hasText(value)) {
            switch (key) {
              case "plate" -> motorcycleService.findByPlate(value).ifPresent(results::add);
              case "brand" ->
                  results.addAll(motorcycleService.findByBrandContainingIgnoreCase(value));
              case "line" ->
                  results.addAll(motorcycleService.findByLineContainingIgnoreCase(value));
              case "model" ->
                  results.addAll(motorcycleService.findByModelContainingIgnoreCase(value));
              case "motorcycleType" ->
                  results.addAll(motorcycleService.findByMotorcycleTypeContainingIgnoreCase(value));
              default -> throw new IllegalArgumentException("Unknown filter: " + key);
            }
          }
        });

    List<MotorcycleResponse> motorcycleResponses =
        results.stream().map(vehicleMapper::toMotorcycleResponse).toList();
    return ResponseEntity.ok(motorcycleResponses);
  }
}

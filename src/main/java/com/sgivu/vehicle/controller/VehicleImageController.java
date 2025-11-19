package com.sgivu.vehicle.controller;

import com.sgivu.vehicle.dto.*;
import com.sgivu.vehicle.mapper.VehicleMapper;
import com.sgivu.vehicle.service.VehicleImageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/vehicles/{vehicleId}/images")
public class VehicleImageController {

  private final VehicleImageService vehicleImageService;
  private final VehicleMapper vehicleMapper;

  public VehicleImageController(
      VehicleImageService vehicleImageService, VehicleMapper vehicleMapper) {
    this.vehicleImageService = vehicleImageService;
    this.vehicleMapper = vehicleMapper;
  }

  @PostMapping("/presigned-upload")
  @PreAuthorize("hasAuthority('vehicle:create')")
  public ResponseEntity<VehicleImagePresignedUploadResponse> createPresignedUploadUrl(
      @PathVariable Long vehicleId,
      @Valid @RequestBody VehicleImagePresignedUploadRequest request) {

    return ResponseEntity.ok(vehicleImageService.createPresignedUploadUrl(vehicleId, request));
  }

  @PostMapping("/confirm-upload")
  @PreAuthorize("hasAuthority('vehicle:create')")
  public ResponseEntity<VehicleImageConfirmUploadResponse> confirmUpload(
      @PathVariable Long vehicleId, @Valid @RequestBody VehicleImageConfirmUploadRequest request) {
    return ResponseEntity.ok(
        vehicleMapper.toVehicleImageConfirmUploadResponse(
            vehicleImageService.confirmUpload(vehicleId, request)));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('vehicle:read')")
  public ResponseEntity<List<VehicleImageResponse>> getImages(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(vehicleImageService.getImagesByVehicle(vehicleId));
  }

  @DeleteMapping("/{imageId}")
  @PreAuthorize("hasAuthority('vehicle:delete')")
  public ResponseEntity<Void> deleteImage(
      @PathVariable Long vehicleId, @PathVariable Long imageId) {
    List<VehicleImageResponse> images = vehicleImageService.getImagesByVehicle(vehicleId);
    if (images == null || images.stream().noneMatch(img -> img.id().equals(imageId))) {
      return ResponseEntity.notFound().build();
    }
    vehicleImageService.deleteImage(imageId);
    return ResponseEntity.noContent().build();
  }
}

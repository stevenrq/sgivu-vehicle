package com.sgivu.vehicle.controller;

import com.sgivu.vehicle.dto.*;
import com.sgivu.vehicle.mapper.VehicleMapper;
import com.sgivu.vehicle.service.VehicleImageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Gestiona las operaciones REST relacionadas con imágenes de vehículos (URLs prefirmadas,
 * confirmaciones de subida, listados y eliminaciones).
 */
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

  /**
   * Genera una URL prefirmada para subir una imagen a S3 directamente desde el cliente.
   *
   * @param vehicleId ID del vehículo
   * @param request payload con tipo de archivo y metadatos
   * @return respuesta con bucket, key y URL
   */
  @PostMapping("/presigned-upload")
  @PreAuthorize("hasAuthority('vehicle:create')")
  public ResponseEntity<VehicleImagePresignedUploadResponse> createPresignedUploadUrl(
      @PathVariable Long vehicleId,
      @Valid @RequestBody VehicleImagePresignedUploadRequest request) {

    return ResponseEntity.ok(vehicleImageService.createPresignedUploadUrl(vehicleId, request));
  }

  /**
   * Confirma que la imagen fue subida correctamente y la registra en base de datos.
   *
   * @param vehicleId ID del vehículo
   * @param request datos de la imagen subida
   * @return metadatos persistidos
   */
  @PostMapping("/confirm-upload")
  @PreAuthorize("hasAuthority('vehicle:create')")
  public ResponseEntity<VehicleImageConfirmUploadResponse> confirmUpload(
      @PathVariable Long vehicleId, @Valid @RequestBody VehicleImageConfirmUploadRequest request) {
    return ResponseEntity.ok(
        vehicleMapper.toVehicleImageConfirmUploadResponse(
            vehicleImageService.confirmUpload(vehicleId, request)));
  }

  /**
   * Lista las imágenes de un vehículo ordenadas por relevancia (primaria primero).
   */
  @GetMapping
  @PreAuthorize("hasAuthority('vehicle:read')")
  public ResponseEntity<List<VehicleImageResponse>> getImages(@PathVariable Long vehicleId) {
    return ResponseEntity.ok(vehicleImageService.getImagesByVehicle(vehicleId));
  }

  /**
   * Elimina la imagen indicada, además de removerla de S3 y reasignar la primaria si aplica.
   */
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

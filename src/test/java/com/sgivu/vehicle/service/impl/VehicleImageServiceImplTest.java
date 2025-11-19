package com.sgivu.vehicle.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.sgivu.vehicle.dto.VehicleImageConfirmUploadRequest;
import com.sgivu.vehicle.dto.VehicleImagePresignedUploadRequest;
import com.sgivu.vehicle.entity.Car;
import com.sgivu.vehicle.entity.VehicleImage;
import com.sgivu.vehicle.repository.VehicleBaseRepository;
import com.sgivu.vehicle.repository.VehicleImageRepository;
import com.sgivu.vehicle.service.S3Service;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ExtendWith(MockitoExtension.class)
class VehicleImageServiceImplTest {

  @Mock private VehicleBaseRepository vehicleBaseRepository;
  @Mock private VehicleImageRepository vehicleImageRepository;
  @Mock private S3Service s3Service;
  @Mock private S3Client s3Client;

  @InjectMocks private VehicleImageServiceImpl service;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(service, "bucket", "test-bucket");
  }

  @Test
  void createPresignedUploadUrl_shouldGenerateKeyWithExtension() {
    when(s3Service.generatePresignedUploadUrl(any(), any(), any(), any()))
        .thenReturn("http://example.com/presigned");

    var response =
        service.createPresignedUploadUrl(
            5L, new VehicleImagePresignedUploadRequest("image/jpeg"));

    assertEquals("test-bucket", response.bucket());
    assertTrue(response.key().startsWith("vehicles/5/"));
    assertTrue(response.key().endsWith(".jpg"));
    verify(s3Service)
        .generatePresignedUploadUrl(eq("test-bucket"), eq(response.key()), any(), eq("image/jpeg"));
  }

  @Test
  void createPresignedUploadUrl_shouldValidateContentType() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.createPresignedUploadUrl(
                1L, new VehicleImagePresignedUploadRequest("application/pdf")));
  }

  @Test
  void confirmUpload_shouldSaveImageAndResetPreviousPrimary() {
    Car vehicle = new Car();
    vehicle.setId(10L);
    VehicleImage previous = new VehicleImage();
    previous.setPrimaryImage(true);
    previous.setVehicle(vehicle);
    vehicle.getImages().add(previous);

    when(vehicleBaseRepository.findById(10L)).thenReturn(Optional.of(vehicle));
    when(vehicleImageRepository.save(any()))
        .thenAnswer(invocation -> (VehicleImage) invocation.getArgument(0));
    when(s3Client.headObject(any(HeadObjectRequest.class)))
        .thenReturn(HeadObjectResponse.builder().build());

    VehicleImageConfirmUploadRequest request =
        new VehicleImageConfirmUploadRequest(
            "car.jpg", "image/jpeg", 1234L, "vehicles/10/new-file.jpg", true);

    VehicleImage saved = service.confirmUpload(10L, request);

    assertTrue(saved.isPrimaryImage());
    assertFalse(previous.isPrimaryImage());
    assertEquals("vehicles/10/new-file.jpg", saved.getKey());
    verify(vehicleImageRepository).save(any(VehicleImage.class));
  }

  @Test
  void confirmUpload_shouldRejectDuplicateFileNameAndCleanupS3() {
    Car vehicle = new Car();
    vehicle.setId(11L);
    when(vehicleBaseRepository.findById(11L)).thenReturn(Optional.of(vehicle));
    when(s3Client.headObject(any(HeadObjectRequest.class)))
        .thenReturn(HeadObjectResponse.builder().build());
    when(vehicleImageRepository.existsByVehicleIdAndFileName(eq(11L), any()))
        .thenReturn(true);

    VehicleImageConfirmUploadRequest request =
        new VehicleImageConfirmUploadRequest(
            "dup.jpg", "image/jpeg", 100L, "vehicles/11/dup.jpg", true);

    assertThrows(IllegalArgumentException.class, () -> service.confirmUpload(11L, request));
    verify(s3Service).deleteObject("test-bucket", "vehicles/11/dup.jpg");
    verify(vehicleImageRepository, never()).save(any());
  }

  @Test
  void deleteImage_shouldAssignNewPrimaryWhenDeletingCurrentPrimary() {
    Car vehicle = new Car();
    vehicle.setId(5L);

    VehicleImage primary = new VehicleImage();
    primary.setId(1L);
    primary.setPrimaryImage(true);
    primary.setVehicle(vehicle);
    primary.setBucket("b");
    primary.setKey("k1");

    VehicleImage secondary = new VehicleImage();
    secondary.setId(2L);
    secondary.setPrimaryImage(false);
    secondary.setVehicle(vehicle);
    secondary.setBucket("b");
    secondary.setKey("k2");

    when(vehicleImageRepository.findById(1L)).thenReturn(Optional.of(primary));
    when(vehicleImageRepository.findByVehicleIdOrderByPrimaryImageDescCreatedAtAsc(5L))
        .thenReturn(List.of(secondary, new VehicleImage()));

    service.deleteImage(1L);

    verify(s3Service).deleteObject("b", "k1");
    verify(vehicleImageRepository).delete(primary);
    assert secondary.isPrimaryImage();
    verify(vehicleImageRepository).saveAll(anyList());
  }
}

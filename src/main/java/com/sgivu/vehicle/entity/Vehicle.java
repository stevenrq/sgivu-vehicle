package com.sgivu.vehicle.entity;

import com.sgivu.vehicle.enums.VehicleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name = "vehicles")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Vehicle implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicles_id_seq")
  @SequenceGenerator(name = "vehicles_id_seq", sequenceName = "vehicles_id_seq", allocationSize = 1)
  private Long id;

  @NotBlank
  @Column(nullable = false, length = 20)
  private String brand;

  @NotBlank
  @Column(nullable = false, length = 20)
  private String model;

  @NotNull
  @Column(nullable = false)
  private Integer capacity;

  @NotBlank
  @Column(nullable = false, length = 20)
  private String line;

  @NotBlank
  @Column(nullable = false, unique = true, length = 10)
  private String plate;

  @NotBlank
  @Column(name = "motor_number", nullable = false, unique = true, length = 30)
  private String motorNumber;

  @NotBlank
  @Column(name = "serial_number", nullable = false, unique = true, length = 30)
  private String serialNumber;

  @NotBlank
  @Column(name = "chassis_number", nullable = false, unique = true, length = 30)
  private String chassisNumber;

  @NotBlank
  @Column(nullable = false, length = 20)
  private String color;

  @NotBlank
  @Column(name = "city_registered", nullable = false, length = 30)
  private String cityRegistered;

  @Min(1950)
  @Max(2050)
  @NotNull
  @Column(nullable = false)
  private Integer year;

  @Min(0)
  @NotNull
  @Column(nullable = false)
  private Integer mileage;

  @NotBlank
  @Column(name = "transmission", nullable = false, length = 20)
  private String transmission;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VehicleStatus status;

  @PositiveOrZero
  @Column(name = "purchase_price", nullable = false)
  private Double purchasePrice;

  @PositiveOrZero
  @Column(name = "sale_price", nullable = false)
  private Double salePrice;

  @Column(name = "photo_url", length = 500)
  private String photoUrl;

  @Column(name = "is_available", nullable = false)
  private boolean isAvailable;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.isAvailable = true;
    if (this.status == null) this.status = VehicleStatus.AVAILABLE;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}

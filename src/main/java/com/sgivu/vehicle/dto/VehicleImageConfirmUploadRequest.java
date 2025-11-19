package com.sgivu.vehicle.dto;

public record VehicleImageConfirmUploadRequest(
    String fileName, String contentType, Long size, String key, Boolean primary) {}

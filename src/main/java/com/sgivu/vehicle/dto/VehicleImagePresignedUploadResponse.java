package com.sgivu.vehicle.dto;

public record VehicleImagePresignedUploadResponse(String bucket, String key, String uploadUrl) {}

package com.sgivu.vehicle.service.impl;

import com.sgivu.vehicle.service.S3Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * Implementación de {@link S3Service} que encapsula toda la interacción con Amazon S3 (creación de
 * buckets, carga/descarga de archivos y generación de URLs prefirmadas). Este servicio es reutilizado
 * por la capa de imágenes de vehículos para delegar la complejidad de S3.
 */
@Service
public class S3ServiceImpl implements S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${spring.destination.folder}")
  private String destinationFolder;

  public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
  }

  /**
   * Crea un bucket en S3 utilizando las credenciales configuradas.
   *
   * @param bucket nombre del bucket a crear
   * @return ubicación devuelta por AWS
   */
  @Override
  public String createBucket(String bucket) {
    CreateBucketResponse createBucketResponse =
        s3Client.createBucket(createBucketRequest -> createBucketRequest.bucket(bucket));

    return "Bucket creado correctamente en " + createBucketResponse.location();
  }

  /**
   * Verifica la existencia del bucket realizando una operación HEAD.
   *
   * @param bucket nombre del bucket
   * @return mensaje indicando si existe o no
   */
  @Override
  public String checkIfBucketExists(String bucket) {
    try {
      s3Client.headBucket(headBucketRequest -> headBucketRequest.bucket(bucket));
      return ("El bucket " + bucket + " existe");
    } catch (S3Exception e) {
      return "El bucket no existe";
    }
  }

  /**
   * Lista todos los buckets asociados a las credenciales actuales.
   *
   * @return nombres de buckets
   */
  @Override
  public List<String> getAllBuckets() {
    ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
    if (listBucketsResponse.hasBuckets()) {
      return listBucketsResponse.buckets().stream().map(Bucket::name).toList();
    } else {
      return List.of();
    }
  }

  /**
   * Carga un archivo a S3.
   *
   * @param bucket bucket destino
   * @param key clave completa dentro del bucket
   * @param fileLocation ruta local del archivo
   * @return {@code true} si la operación fue exitosa
   */
  @Override
  public Boolean uploadFile(String bucket, String key, Path fileLocation) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();
    PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, fileLocation);

    return putObjectResponse.sdkHttpResponse().isSuccessful();
  }

  /**
   * Descarga un archivo desde S3 y lo guarda en el directorio configurado. Crea directorios si no
   * existen.
   *
   * @param bucket bucket origen
   * @param key clave del objeto
   * @throws IOException si falla la escritura local
   */
  @Override
  public void downloadFile(String bucket, String key) throws IOException {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(getObjectRequest);

    // Obtener solo el nombre real del archivo
    String filename = key.contains("/") ? key.substring(key.lastIndexOf("/") + 1) : key;

    Path filePath = Paths.get(destinationFolder, filename);
    File parentDir = filePath.getParent().toFile();

    // Crear directorios si no existen
    if (!parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("No se pudo crear el directorio: " + parentDir.getAbsolutePath());
    }

    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
      fos.write(bytes.asByteArray());
    } catch (IOException e) {
      throw new IOException("Error descargando archivo desde S3", e);
    }
  }

  /**
   * Genera una URL prefirmada para subir archivos directamente a S3.
   *
   * @param bucket bucket destino
   * @param key clave objetivo
   * @param duration vigencia de la URL
   * @param contentType tipo de contenido esperado
   * @return URL firmada
   */
  @Override
  public String generatePresignedUploadUrl(
      String bucket, String key, Duration duration, String contentType) {
    PresignedPutObjectRequest presignedRequest =
        s3Presigner.presignPutObject(
            builder ->
                builder
                    .signatureDuration(duration)
                    .putObjectRequest(po -> po.bucket(bucket).key(key).contentType(contentType)));

    return presignedRequest.url().toString();
  }

  /**
   * Genera una URL prefirmada para descargar objetos desde S3.
   *
   * @param bucket bucket origen
   * @param key clave del objeto
   * @param duration vigencia de la URL
   * @return URL firmada de descarga
   */
  @Override
  public String generatePresignedDownloadUrl(String bucket, String key, Duration duration) {
    PresignedGetObjectRequest presignedRequest =
        s3Presigner.presignGetObject(
            builder ->
                builder
                    .signatureDuration(duration)
                    .getObjectRequest(go -> go.bucket(bucket).key(key)));

    return presignedRequest.url().toString();
  }

  /**
   * Elimina un objeto del bucket.
   *
   * @param bucket bucket origen
   * @param key clave del objeto
   */
  @Override
  public void deleteObject(String bucket, String key) {
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucket).key(key).build();
    s3Client.deleteObject(deleteObjectRequest);
  }
}

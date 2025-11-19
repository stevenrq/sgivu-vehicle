package com.sgivu.vehicle.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface S3Service {

  /**
   * Crea un nuevo bucket en el almacenamiento S3.
   *
   * @param bucket nombre del bucket a crear
   * @return el nombre del bucket creado
   */
  String createBucket(String bucket);

  /**
   * Verifica si un bucket existe.
   *
   * @param bucket nombre del bucket a validar
   * @return mensaje indicando si existe o no
   */
  String checkIfBucketExists(String bucket);

  /**
   * Obtiene la lista de todos los buckets disponibles.
   *
   * @return lista de nombres de buckets
   */
  List<String> getAllBuckets();

  /**
   * Sube un archivo al bucket y clave especificados.
   *
   * @param bucket nombre del bucket destino
   * @param key clave (key) del archivo dentro del bucket
   * @param fileLocation ruta local del archivo a subir
   * @return true si la carga fue exitosa, false en caso contrario
   */
  Boolean uploadFile(String bucket, String key, Path fileLocation);

  /**
   * Descarga un archivo del bucket utilizando la clave indicada. El archivo se guarda localmente en
   * la ruta definida por la implementación.
   *
   * @param bucket nombre del bucket origen
   * @param key clave del archivo a descargar
   * @throws IOException si ocurre un error al escribir el archivo localmente
   */
  void downloadFile(String bucket, String key) throws IOException;

  /**
   * Genera una URL prefirmada para cargar un archivo.
   *
   * @param bucket bucket destino
   * @param key clave del archivo
   * @param duration duración de validez de la URL
   * @return URL prefirmada para carga
   */
  String generatePresignedUploadUrl(
      String bucket, String key, Duration duration, String contentType);

  /**
   * Genera una URL prefirmada para descargar un archivo.
   *
   * @param bucket bucket origen
   * @param key clave del archivo a descargar
   * @param duration duración de validez de la URL
   * @return URL prefirmada para descarga
   */
  String generatePresignedDownloadUrl(String bucket, String key, Duration duration);

  /**
   * Elimina un objeto del bucket especificado.
   *
   * @param bucket nombre del bucket
   * @param key clave del objeto a eliminar
   */
  void deleteObject(String bucket, String key);
}

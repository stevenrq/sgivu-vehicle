package com.sgivu.vehicle.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.GetBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.GetBucketCorsResponse;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

@ExtendWith(MockitoExtension.class)
class S3BucketCorsConfigTest {

  @Mock private S3Client s3Client;

  @Test
  void ensureBucketCors_shouldConfigureRuleWhenMissing() {
    when(s3Client.getBucketCors(any(GetBucketCorsRequest.class)))
        .thenReturn(GetBucketCorsResponse.builder().corsRules(List.of()).build());

    S3BucketCorsConfig config =
        new S3BucketCorsConfig(s3Client, "bucket-test", "http://localhost:4200");

    config.ensureBucketCors();

    verify(s3Client)
        .putBucketCors(
            org.mockito.ArgumentMatchers.argThat(
                requestMatchesRule("http://localhost:4200", List.of("PUT", "POST", "DELETE", "HEAD"))));
  }

  @Test
  void ensureBucketCors_shouldSkipUpdateWhenRuleAlreadyPresent() {
    CORSRule existingRule =
        CORSRule.builder()
            .allowedOrigins(List.of("http://localhost:4200"))
            .allowedMethods("GET", "PUT", "POST", "DELETE", "HEAD")
            .allowedHeaders(List.of("*"))
            .build();
    when(s3Client.getBucketCors(any(GetBucketCorsRequest.class)))
        .thenReturn(GetBucketCorsResponse.builder().corsRules(List.of(existingRule)).build());

    S3BucketCorsConfig config =
        new S3BucketCorsConfig(s3Client, "bucket-test", "http://localhost:4200");

    config.ensureBucketCors();

    verify(s3Client, never()).putBucketCors(any(PutBucketCorsRequest.class));
  }

  private ArgumentMatcher<PutBucketCorsRequest> requestMatchesRule(
      String origin, List<String> methods) {
    return request -> {
      var rules = request.corsConfiguration().corsRules();
      if (rules.isEmpty()) return false;
      CORSRule rule = rules.get(rules.size() - 1);
      assertThat(rule.allowedOrigins()).contains(origin);
      assertThat(rule.allowedMethods()).containsAll(methods).contains("GET");
      assertThat(rule.allowedHeaders()).contains("*");
      return true;
    };
  }
}

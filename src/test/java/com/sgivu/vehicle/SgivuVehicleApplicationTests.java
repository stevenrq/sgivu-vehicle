package com.sgivu.vehicle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Se omite en pruebas locales sin config-server disponible")
@SpringBootTest(
    properties = {
      "spring.cloud.config.enabled=false",
      "spring.config.import=optional:configserver:http://sgivu-config:8888"
    })
class SgivuVehicleApplicationTests {

  @Test
  void contextLoads() {}

}

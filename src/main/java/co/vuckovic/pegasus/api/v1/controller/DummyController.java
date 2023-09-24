package co.vuckovic.pegasus.api.v1.controller;

import co.vuckovic.pegasus.service.DummyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy")
@RequiredArgsConstructor
public class DummyController {

  private final DummyService dummyService;

  @GetMapping
  public ResponseEntity<Void> populateMicroserviceDB(){
    dummyService.populateMicroserviceDB();
    return ResponseEntity.ok().build();
  }
}

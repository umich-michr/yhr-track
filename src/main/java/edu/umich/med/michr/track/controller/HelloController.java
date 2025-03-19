package edu.umich.med.michr.track.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloController {
  private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
  @GetMapping
  public String get(){
    logger.info("Received request for /");

    logger.debug("Debugging details for request handling...");
    return "Hi there!";
  }
}

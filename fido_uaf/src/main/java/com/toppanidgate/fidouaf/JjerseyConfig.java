package com.toppanidgate.fidouaf;

import com.toppanidgate.fidouaf.res.FidoUafResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JjerseyConfig extends ResourceConfig {
  public JjerseyConfig () {
    register(FidoUafResource.class);
  }
}
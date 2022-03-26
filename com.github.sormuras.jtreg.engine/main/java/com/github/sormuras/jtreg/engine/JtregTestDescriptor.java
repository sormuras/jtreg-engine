package com.github.sormuras.jtreg.engine;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

class JtregTestDescriptor extends AbstractTestDescriptor {

  private final JtregService service;

  JtregTestDescriptor(UniqueId parent, JtregService service, String displayName) {
    super(parent.append("test", service.getClass().getCanonicalName()), displayName);
    this.service = service;
  }

  JtregService getService() {
    return service;
  }

  @Override
  public Type getType() {
    return Type.TEST;
  }
}

package com.github.sormuras.jtreg.engine;

import org.junit.platform.commons.annotation.Testable;
import java.nio.file.Path;

@Testable
public interface JtregService {

  default String name() {
    return getClass().getSimpleName();
  }

  default void run(Path jtregHome) throws Exception {
    var name = name();
    var work = "build/%s/work".formatted(name);
    var report = "build/%s/report".formatted(name);
    var test = "test/%s".formatted(name);
    run(jtregHome, "-w:" + work, "-r:" + report, test);
  }

  default void verify() throws Exception {}

  static String run(Path jtregHome,  String... args) throws Exception {
    return JtregEngine.run(jtregHome, args);
  }
}

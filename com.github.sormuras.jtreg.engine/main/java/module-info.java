module com.github.sormuras.jtreg.engine {
  exports com.github.sormuras.jtreg.engine;

  requires org.junit.platform.engine;

  uses com.github.sormuras.jtreg.engine.JtregService;

  provides org.junit.platform.engine.TestEngine with
      com.github.sormuras.jtreg.engine.JtregEngine;
}

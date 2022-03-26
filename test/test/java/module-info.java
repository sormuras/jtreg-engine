module test {
  requires com.github.sormuras.jtreg.engine;

  provides com.github.sormuras.jtreg.engine.JtregService with
      test.TestSimpleCheck,
      test.TestSimpleCheck2;
}

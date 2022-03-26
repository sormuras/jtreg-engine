package com.github.sormuras.jtreg.engine;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

public class JtregEngine implements TestEngine {

  @Override
  public String getId() {
    return "jtreg-engine";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
    var configuration = engineDiscoveryRequest.getConfigurationParameters();
    var jtregHome = computeJtregHome(configuration);

    var engineDescriptor = new EngineDescriptor(uniqueId, "JTReg Engine: " + jtregHome);

    // All in package  : ClasspathRootSelector [classpathRoot = file:///.../.idea/out/test/test/]
    // All in directory: ClasspathRootSelector [classpathRoot = file:///.../.idea/out/test/test/]
    // Class (X)       : ClassSelector [className = 'test.TestSimpleCheck']
    var classSelectors =
        engineDiscoveryRequest.getSelectorsByType(ClassSelector.class).stream()
            .map(ClassSelector::getJavaClass)
            .toList();

    ServiceLoader.load(JtregService.class).stream()
        .filter(
            provider -> {
              if (classSelectors.isEmpty()) return true;
              return classSelectors.contains(provider.type());
            })
        .map(ServiceLoader.Provider::get)
        .map(service -> new JtregTestDescriptor(uniqueId, service, service.toString()))
        .forEach(engineDescriptor::addChild);

    return engineDescriptor;
  }

  @Override
  public void execute(ExecutionRequest executionRequest) {
    var configuration = executionRequest.getConfigurationParameters();
    var jtregHome = computeJtregHome(configuration);

    var engine = executionRequest.getRootTestDescriptor();
    var listener = executionRequest.getEngineExecutionListener();
    listener.executionStarted(engine);
    for (var child : engine.getChildren()) {
      listener.executionStarted(child);
      try {
        if (child instanceof JtregTestDescriptor test) {
          var service = test.getService();
          service.run(jtregHome);
          service.verify();
        } else {
          throw new UnsupportedOperationException("Unsupported descriptor type: " + child);
        }
      } catch (Exception e) {
        listener.executionFinished(child, TestExecutionResult.failed(e));
        continue;
      }
      listener.executionFinished(child, TestExecutionResult.successful());
    }
    listener.executionFinished(engine, TestExecutionResult.successful());
  }

  public static Path computeJtregHome(ConfigurationParameters configuration) {
    var localHome = ".bach/external-programs/jtreg";
    var jtregHomeEnv = System.getenv().getOrDefault("JTREG_HOME", localHome);
    return Path.of(configuration.get("jtreg.home").orElse(jtregHomeEnv)).toAbsolutePath();
  }

  public static String run(Path home, String... args) throws Exception {
    var builder = new ProcessBuilder("java");
    builder.command().add("--class-path");
    builder.command().add(home.resolve("lib") + File.separator + "*");
    builder.command().add("com.sun.javatest.regtest.Main");
    builder.command().addAll(List.of(args));
    builder.environment().put("JTREG_HOME", home.toString());
    var process = builder.inheritIO().start();
    var result = process.waitFor();
    if (result != 0) throw new RuntimeException("jtreg exited with error code " + result);
    return process.toString();
  }
}

package org.robolectric.internal;

import org.jetbrains.annotations.NotNull;
import org.robolectric.internal.bytecode.ShadowInvalidator;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourceExtractor;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceParser;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;

import java.util.HashMap;
import java.util.Map;

public class SdkEnvironment {
  private final SdkConfig sdkConfig;
  private final ClassLoader robolectricClassLoader;
  private final ShadowInvalidator shadowInvalidator;
  private ShadowMap shadowMap = ShadowMap.EMPTY;
  private ResourceTable systemResourceTable;

  public SdkEnvironment(SdkConfig sdkConfig, ClassLoader robolectricClassLoader) {
    this.sdkConfig = sdkConfig;
    this.robolectricClassLoader = robolectricClassLoader;
    shadowInvalidator = new ShadowInvalidator();
  }

  public synchronized ResourceTable getSystemResourceTable(DependencyResolver dependencyResolver) {
    if (systemResourceTable == null) {
      ResourcePath resourcePath = createRuntimeSdkResourcePath(dependencyResolver);
      ResourceIndex resourceIndex = new ResourceIndex(resourcePath.getPackageName());
      ResourceExtractor.populate(resourcePath, resourceIndex);
      systemResourceTable = new ResourceTable(resourceIndex);
      ResourceParser.load(resourcePath, systemResourceTable);
    }
    return systemResourceTable;
  }

  @NotNull
  private ResourcePath createRuntimeSdkResourcePath(DependencyResolver dependencyResolver) {
    try {
      Fs systemResFs = Fs.fromJar(dependencyResolver.getLocalArtifactUrl(sdkConfig.getAndroidSdkDependency()));
      Class<?> androidRClass = getRobolectricClassLoader().loadClass("android.R");
      Class<?> androidInternalRClass = getRobolectricClassLoader().loadClass("com.android.internal.R");
      return new ResourcePath(androidRClass,
          androidRClass.getPackage().getName(),
          systemResFs.join("res"), systemResFs.join("assets"),
          androidInternalRClass);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public Class<?> bootstrappedClass(Class<?> testClass) {
    try {
      return robolectricClassLoader.loadClass(testClass.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public ClassLoader getRobolectricClassLoader() {
    return robolectricClassLoader;
  }

  public ShadowInvalidator getShadowInvalidator() {
    return shadowInvalidator;
  }

  public SdkConfig getSdkConfig() {
    return sdkConfig;
  }

  public ShadowMap replaceShadowMap(ShadowMap shadowMap) {
    ShadowMap oldMap = this.shadowMap;
    this.shadowMap = shadowMap;
    return oldMap;
  }
}

package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;

/**
 * A resource loader with no resources.
 */
public class EmptyResourceProvider extends XResourceProvider {
  private String packageName;

  public EmptyResourceProvider() {
    this(null, null);
  }

  public EmptyResourceProvider(String packageName, ResourceIndex resourceIndex) {
    super(resourceIndex);
    this.packageName = packageName;
  }

  @Override
  void doInitialize() {
  }

  @Override
  public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return null;
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    return null;
  }

  @Override
  public boolean providesFor(String namespace) {
    return packageName.equals(namespace);
  }
}

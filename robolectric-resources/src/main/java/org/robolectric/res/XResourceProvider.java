package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.IOException;
import java.io.InputStream;

// TODO: Give me a better name
abstract class XResourceProvider extends ResourceProvider {
  final ResBunch data = new ResBunch();
  final ResBundle xmlDocuments = new ResBundle();
  final ResBundle rawResources = new ResBundle();
  
  private final ResourceIndex resourceIndex;
  private boolean isInitialized = false;

  XResourceProvider(ResourceIndex resourceIndex) {
    this.resourceIndex = resourceIndex;
  }

  abstract void doInitialize();

  synchronized void initialize() {
    if (isInitialized) return;
    doInitialize();
    isInitialized = true;

    makeImmutable();
  }

  private void makeImmutable() {
    data.makeImmutable();

    xmlDocuments.makeImmutable();
    rawResources.makeImmutable();
  }


  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override
  public void receive(Visitor visitor) {
    initialize();
    data.receive(visitor);
  }
}

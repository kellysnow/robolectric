package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.res.builder.XmlBlock;

public class RoutingResourceProvider extends ResourceProvider {
  public static final ResourceTable EMPTY_RESOURCE_TABLE = new ResourceTable(new ResourceIndex(""));
  private final Map<String, ResourceTable> resourceTables;
  private final ResourceIndex resourceIndex;

  public RoutingResourceProvider(ResourceTable... resourceTables) {
    this.resourceTables = new HashMap<>();

    Set<ResourceIndex> resourceIndexes = new HashSet<>();
    for (ResourceTable resourceTable : resourceTables) {
      this.resourceTables.put(resourceTable.getPackageName(), resourceTable);
      resourceIndexes.add(resourceTable.getResourceIndex());
    }
    resourceIndex = new MergedResourceIndex(resourceIndexes.toArray(new ResourceIndex[resourceIndexes.size()]));
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return pickFor(resName).getValue(resName, qualifiers);
  }

  @Override
  public XmlBlock getXml(ResName resName, String qualifiers) {
    return pickFor(resName).getXml(resName, qualifiers);
  }

  @Override
  public InputStream getRawValue(ResName resName) {
    return pickFor(resName).getRawValue(resName);
  }

  @Override
  public ResourceIndex getResourceIndex() {
    return resourceIndex;
  }

  @Override public boolean providesFor(String namespace) {
    return whichProvidesFor(namespace) != null;
  }

  @Override
  public void receive(Visitor visitor) {
    for (ResourceTable resourceTable : resourceTables.values()) {
      resourceTable.data.receive(visitor);
    }
  }

  private ResourceTable pickFor(int id) {
    ResName resName = resourceIndex.getResName(id);
    return pickFor(resName);
  }

  private ResourceTable pickFor(ResName resName) {
    if (resName == null) return EMPTY_RESOURCE_TABLE;
    return pickFor(resName.packageName);
  }

  private ResourceTable pickFor(String namespace) {
    if (namespace.equals("android.internal")) {
      return EMPTY_RESOURCE_TABLE;
    }
    ResourceTable resourceTable = resourceTables.get(namespace);
    if (resourceTable == null) {
      resourceTable = whichProvidesFor(namespace);
      return (resourceTable != null) ? resourceTable : EMPTY_RESOURCE_TABLE;
    }
    return resourceTable;
  }

  private ResourceTable whichProvidesFor(String namespace) {
    for (ResourceTable resourceTable : resourceTables.values()) {
      if (resourceTable.getPackageName().equals(namespace)) {
        return resourceTable;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "RoutingResourceProvider{" + resourceTables + "}";
  }
}

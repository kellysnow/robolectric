package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.manifest.AndroidManifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceMerger {
  @NotNull
  public static ResourceTable buildResourceTable(AndroidManifest appManifest) {
    String packageName = appManifest.getPackageName();
    ResourcePath appResourcePath = appManifest.getResourcePath();
    List<ResourcePath> allResourcePaths = appManifest.getIncludedResourcePaths();

    return buildResourceTable(packageName, appResourcePath, allResourcePaths);
  }

  @NotNull
  public static ResourceTable buildResourceTable(String packageName, ResourcePath appResourcePath, List<ResourcePath> allResourcePaths) {
    ResourceIndex resourceIndex = new ResourceIndex(packageName);
    ResourceTable resourceTable = new ResourceTable(resourceIndex);
    ResourceExtractor.populate(appResourcePath, resourceIndex);

    List<ResourcePath> reversed = new ArrayList<>(allResourcePaths);
    Collections.reverse(reversed);
    for (ResourcePath resourcePath : reversed) {
      ResourceParser.load(resourcePath, resourceTable);
    }
    return resourceTable;
  }
}

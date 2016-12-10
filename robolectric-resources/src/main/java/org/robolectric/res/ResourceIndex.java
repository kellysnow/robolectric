package org.robolectric.res;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ResourceIndex {
  private static final Logger LOGGER = Logger.getLogger(ResourceExtractor.class.getName());

  protected final Map<ResName, Integer> resourceNameToId = new HashMap<>();
  protected final Map<Integer, ResName> resourceIdToResName = new HashMap<>();

  private Integer maxUsedInt = null;
  private Integer generatedIdStart = null;
  private String packageName;

  public ResourceIndex(String packageName) {
    this.packageName = packageName;
  }

  public synchronized Integer getResourceId(ResName resName) {
    Integer id = resourceNameToId.get(resName);
    if (id == null && ("android".equals(resName.packageName) || "".equals(resName.packageName))) {
      if (maxUsedInt == null) {
        maxUsedInt = resourceIdToResName.isEmpty() ? 0 : Collections.max(resourceIdToResName.keySet());
        generatedIdStart = maxUsedInt;
      }
      id = ++maxUsedInt;
      resourceNameToId.put(resName, id);
      resourceIdToResName.put(id, resName);
      LOGGER.fine("no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    if (id == null) return 0;

    return id;
  }

  public synchronized ResName getResName(int resourceId) {
    return resourceIdToResName.get(resourceId);
  }

  public String getPackageName() {
    return packageName;
  }
}

package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ResourceExtractor {

  public static void populate(ResourcePath resourcePath, ResourceIndex resourceIndex) {
    String packageName = resourcePath.getPackageName();
    if (resourcePath.getRClass() != null) {
      populate(resourcePath.getRClass(), packageName, resourceIndex);
    }

    if (resourcePath.getInternalRClass() != null) {
      populate(resourcePath.getInternalRClass(), packageName, resourceIndex);
    }
  }

  private static void populate(Class<?> rClass, String packageName, ResourceIndex resourceIndex) {
    for (Class innerClass : rClass.getClasses()) {
      for (Field field : innerClass.getDeclaredFields()) {
        if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
          String section = innerClass.getSimpleName();
          int id;
          try {
            id = field.getInt(null);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }

          if (!section.equals("styleable")) {
            String fieldName = field.getName();
            ResName resName = new ResName(packageName, section, fieldName);

            resourceIndex.resourceNameToId.put(resName, id);
            resourceIndex.resourceIdToResName.put(id, resName);
          }
        }
      }
    }
  }
}
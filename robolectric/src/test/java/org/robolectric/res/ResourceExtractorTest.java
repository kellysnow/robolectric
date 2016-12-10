package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.*;

public class ResourceExtractorTest {
  private ResourceIndex resourceIndex;

  @Before
  public void setUp() throws Exception {
    resourceIndex = ResourceMerger.buildResourceTable("app", testResources(), asList(systemResources(), testResources())).getResourceIndex();
    ResourceIndex resourceIndex1 = new ResourceIndex("app");
    ResourceExtractor.populate(systemResources(), resourceIndex1);
    ResourceIndex resourceIndex2 = new ResourceIndex("lib");
    ResourceExtractor.populate(testResources(), resourceIndex2);
    resourceIndex = new MergedResourceIndex(resourceIndex2, resourceIndex1);
  }

  @Test
  public void shouldHandleStyleable() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "id/textStyle", R.class.getPackage().getName())).isEqualTo(R.id.textStyle);
    assertThat(ResName.getResourceId(resourceIndex, "styleable/TitleBar_textStyle", R.class.getPackage().getName())).isEqualTo(0);
  }

  @Test
  public void shouldPrefixResourcesWithPackageContext() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "id/text1", "android")).isEqualTo(android.R.id.text1);
    assertThat(ResName.getResourceId(resourceIndex, "id/text1", R.class.getPackage().getName())).isEqualTo(R.id.text1);
  }

  @Test
  public void shouldPrefixAllSystemResourcesWithAndroid() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, "android:id/text1", "android")).isEqualTo(android.R.id.text1);
  }

  @Test
  public void shouldHandleNull() throws Exception {
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "android")).isEqualTo(null);
    assertThat(ResName.getResourceId(resourceIndex, AttributeResource.NULL_VALUE, "anything")).isEqualTo(null);
  }

  @Test
  public void shouldRetainPackageNameForFullyQualifiedQueries() throws Exception {
    assertThat(resourceIndex.getResName(android.R.id.text1).getFullyQualifiedName()).isEqualTo("android:id/text1");
    assertThat(resourceIndex.getResName(R.id.burritos).getFullyQualifiedName()).isEqualTo("org.robolectric:id/burritos");
  }

  @Test
  public void shouldResolveEquivalentResNames() throws Exception {
    ResourceIndex resourceIndex2 = new ResourceIndex("packageName");
    ResourceExtractor.populate(lib3Resources(), resourceIndex2);
    ResourceIndex resourceIndex3 = new ResourceIndex("packageName");
    ResourceExtractor.populate(lib2Resources(), resourceIndex3);
    ResourceIndex resourceIndex4 = new ResourceIndex("packageName");
    ResourceExtractor.populate(lib1Resources(), resourceIndex4);
    ResourceIndex resourceIndex5 = new ResourceIndex("packageName");
    ResourceExtractor.populate(testResources(), resourceIndex5);
    OverlayResourceIndex overlayResourceIndex = new OverlayResourceIndex(
        "org.robolectric",
        resourceIndex5,
        resourceIndex4,
        resourceIndex3,
        resourceIndex2);
    ResourceIndex resourceIndex1 = new ResourceIndex("packageName");
    ResourceExtractor.populate(systemResources(), resourceIndex1);
    resourceIndex = new MergedResourceIndex(overlayResourceIndex, resourceIndex1);

    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib1", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib2", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
    assertThat(resourceIndex.getResourceId(new ResName("org.robolectric.lib3", "string", "in_all_libs"))).isEqualTo(R.string.in_all_libs);
  }
}

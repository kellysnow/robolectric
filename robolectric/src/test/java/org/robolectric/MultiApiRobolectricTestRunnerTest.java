package org.robolectric;

import android.os.Build;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class MultiApiRobolectricTestRunnerTest {

  private final static ImmutableSet<Integer> APIS_FOR_TEST = ImmutableSet.of(
      JELLY_BEAN,
      JELLY_BEAN_MR1,
      JELLY_BEAN_MR2,
      KITKAT,
      LOLLIPOP,
      LOLLIPOP_MR1,
      M);

  private MultiApiRobolectricTestRunner runner;
  private Properties properties;
  private RunNotifier runNotifier;
  private MyRunListener runListener;

  private int numSupportedApis;

  @Before
  public void setUp() {
    numSupportedApis = APIS_FOR_TEST.size();
    properties = new Properties();

    runListener = new MyRunListener();
    runNotifier = new RunNotifier();
    runNotifier.addListener(runListener);
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestWithNoConfig.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(allApisForTest());
  }

  @NotNull
  private static Integer[] allApisForTest() {
    return APIS_FOR_TEST.toArray(new Integer[APIS_FOR_TEST.size()]);
  }

  @Test
  public void withEnabledApis_createChildrenForEachSupportedApi() throws Throwable {
    properties.setProperty("robolectric.enabledApis", "16,17");
    runner = new MultiApiRobolectricTestRunner(TestWithNoConfig.class, APIS_FOR_TEST, properties);
    assertThat(runner.getChildren()).hasSize(2);
  }

  @Test
  public void noConfig() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestWithNoConfig.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(allApisForTest());
    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    assertThat(runListener.finished).hasSize(numSupportedApis);
  }

  @Test
  public void classConfigWithSdkGroup() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestClassConfigWithSdkGroup.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for JellyBean and Lollipop
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void methodConfigWithSdkGroup() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestMethodConfigWithSdkGroup.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for JellyBean and Lollipop
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void classConfigMinSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestClassLollipopAndUp.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void classConfigMaxSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestClassUpToAndIncludingLollipop.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 5;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void classConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestClassBetweenJellyBeanMr2AndLollipop.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Since test method should only be run once
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyBetweenJellyBeanMr2AndLollipop);
  }

  @Test
  public void methodConfigMinSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestMethodLollipopAndUp.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void methodConfigMaxSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestMethodUpToAndIncludingLollipop.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 5;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void methodConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = new MultiApiRobolectricTestRunner(TestMethodBetweenJellyBeanMr2AndLollipop.class, APIS_FOR_TEST, properties);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyBetweenJellyBeanMr2AndLollipop);
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public static class TestWithNoConfig {
    @Test public void test() {}
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(sdk = {JELLY_BEAN, LOLLIPOP})
  public static class TestClassConfigWithSdkGroup {
    @Test public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isIn(JELLY_BEAN, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public static class TestMethodConfigWithSdkGroup {
    @Config(sdk = {JELLY_BEAN, LOLLIPOP})
    @Test public void testShouldRunApi16() {
      assertThat(Build.VERSION.SDK_INT).isIn(JELLY_BEAN, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(minSdk = LOLLIPOP)
  public static class TestClassLollipopAndUp {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isGreaterThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(maxSdk = LOLLIPOP)
  public static class TestClassUpToAndIncludingLollipop {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isLessThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
  public static class TestClassBetweenJellyBeanMr2AndLollipop {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isBetween(JELLY_BEAN_MR2, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public static class TestMethodLollipopAndUp {
    @Config(minSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isGreaterThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public static class TestMethodUpToAndIncludingLollipop {
    @Config(maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isLessThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public static class TestMethodBetweenJellyBeanMr2AndLollipop {
    @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isBetween(JELLY_BEAN_MR2, LOLLIPOP);
    }
  }

  private static List<Integer> apisFor(List<FrameworkMethod> children) {
    List<Integer> apis = new ArrayList<>();
    for (FrameworkMethod child : children) {
      apis.add(((RobolectricTestRunner.RobolectricFrameworkMethod) child).apiLevel);
    }
    return apis;
  }

  private static class MyRunListener extends RunListener {
    private List<String> started = new ArrayList<>();
    private List<String> finished = new ArrayList<>();
    private List<String> ignored = new ArrayList<>();

    @Override
    public void testStarted(Description description) throws Exception {
      started.add(description.getDisplayName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
      finished.add(description.getDisplayName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
      ignored.add(description.getDisplayName());
    }
  }
}

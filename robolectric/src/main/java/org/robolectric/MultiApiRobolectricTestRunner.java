package org.robolectric;

import org.robolectric.internal.SdkConfig;

import java.util.Properties;
import java.util.Set;

/**
 * A test runner for Robolectric that will run a test against multiple API versions.
 *
 * @deprecated Use {@link RobolectricTestRunner} instead.
 */
public class MultiApiRobolectricTestRunner extends RobolectricTestRunner {
  public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
    this(klass, SdkConfig.getSupportedApis(), System.getProperties());
  }

  MultiApiRobolectricTestRunner(Class<?> klass, Set<Integer> supportedApis, Properties properties) throws Throwable {
    super(klass, supportedApis, properties);

   }
}

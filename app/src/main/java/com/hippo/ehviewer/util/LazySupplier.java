/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.util;

/*
 * Created by Hippo on 1/15/2017.
 */

import android.support.annotation.NonNull;

/**
 * Lazy initialization for non-static value.
 * <p>
 * Implements {@link #onGet()} to initialize the value.
 * Calls {@link #get()} to get the value.
 * <p>
 * {@link #onGet()} is only called at the first time
 * that {@link #get()} called.
 */
// https://github.com/google/guava/blob/v21.0/guava/src/com/google/common/base/Suppliers.java
public abstract class LazySupplier<T> implements Supplier<T> {

  volatile boolean initialized;
  // "value" does not need to be volatile; visibility piggy-backs
  // on volatile read of "initialized".
  T value;

  public abstract T onGet();

  @Override
  public T get() {
    // A 2-field variant of Double Checked Locking.
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          T t = onGet();
          value = t;
          initialized = true;
          return t;
        }
      }
    }
    return value;
  }

  /**
   * Creates a {@code LazySupplier} from a {@code Supplier}.
   * Returns the supplier itself if it's a {@code LazySupplier}.
   */
  public static <T> LazySupplier<T> from(@NonNull Supplier<T> supplier) {
    return supplier instanceof LazySupplier
        ? (LazySupplier<T>) supplier
        : new DelegateLazySupplier<>(supplier);
  }

  private static class DelegateLazySupplier<T> extends LazySupplier<T> {

    private Supplier<T> supplier;

    public DelegateLazySupplier(Supplier<T> supplier) {
      this.supplier = supplier;
    }

    @Override
    public T onGet() {
      return supplier.get();
    }
  }
}

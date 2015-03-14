/*
 * #%L
 * Java Fork Join Example
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2015 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.javacreed.examples.concurrency.part4;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirSize {

  private static class SizeOfFileAction extends RecursiveAction {

    private static final long serialVersionUID = -196522408291343951L;

    private final File file;
    private final AtomicLong sizeAccumulator;

    public SizeOfFileAction(final File file, final AtomicLong sizeAccumulator) {
      this.file = Objects.requireNonNull(file);
      this.sizeAccumulator = Objects.requireNonNull(sizeAccumulator);
    }

    @Override
    protected void compute() {
      DirSize.LOGGER.debug("Computing size of: {}", file);

      if (file.isFile()) {
        sizeAccumulator.addAndGet(file.length());
      } else {
        final File[] children = file.listFiles();
        if (children != null) {
          for (final File child : children) {
            ForkJoinTask.invokeAll(new SizeOfFileAction(child, sizeAccumulator));
          }
        }
      }
    }
  }

  public static long sizeOf(final File file) {
    final ForkJoinPool pool = new ForkJoinPool();
    try {
      final AtomicLong sizeAccumulator = new AtomicLong();
      pool.invoke(new SizeOfFileAction(file, sizeAccumulator));
      return sizeAccumulator.get();
    } finally {
      pool.shutdown();
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DirSize.class);

  private DirSize() {}

}

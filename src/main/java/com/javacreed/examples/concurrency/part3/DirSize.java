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
package com.javacreed.examples.concurrency.part3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example is broken and suffers from deadlock and is only included for documentation purpose.
 *
 * @author Albert Attard
 *
 */
public class DirSize {

  private static class SizeOfFileCallable implements Callable<Long> {

    private final File file;
    private final ExecutorService executor;

    public SizeOfFileCallable(final File file, final ExecutorService executor) {
      this.file = Objects.requireNonNull(file);
      this.executor = Objects.requireNonNull(executor);
    }

    @Override
    public Long call() throws Exception {
      DirSize.LOGGER.debug("Computing size of: {}", file);
      long size = 0;

      if (file.isFile()) {
        size = file.length();
      } else {
        final List<Future<Long>> futures = new ArrayList<>();
        for (final File child : file.listFiles()) {
          futures.add(executor.submit(new SizeOfFileCallable(child, executor)));
        }

        for (final Future<Long> future : futures) {
          size += future.get();
        }
      }

      return size;
    }
  }

  public static <T> long sizeOf(final File file) {
    final int threads = Runtime.getRuntime().availableProcessors();
    DirSize.LOGGER.debug("Creating executor with {} threads", threads);
    final ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      return executor.submit(new SizeOfFileCallable(file, executor)).get();
    } catch (final Exception e) {
      throw new RuntimeException("Failed to calculate the dir size", e);
    } finally {
      executor.shutdown();
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DirSize.class);

  private DirSize() {}

}

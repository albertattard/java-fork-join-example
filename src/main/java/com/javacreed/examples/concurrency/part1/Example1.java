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
package com.javacreed.examples.concurrency.part1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javacreed.examples.concurrency.utils.FilePath;

public class Example1 {

  public static void main(final String[] args) {
    final long start = System.nanoTime();
    final long size = DirSize.sizeOf(FilePath.TEST_DIR);
    final long taken = System.nanoTime() - start;

    Example1.LOGGER.info("Size of '{}': {} bytes (in {} nano)", FilePath.TEST_DIR, size, taken);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(Example1.class);
}

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
package com.javacreed.examples.concurrency.utils;

import java.util.ArrayList;
import java.util.List;

public class Results {

  private final List<Long> timeTaken = new ArrayList<>();
  private long startTime;

  public long endTime() {
    final long taken = System.nanoTime() - startTime;
    timeTaken.add(taken);
    return taken;
  }

  public long getAverageTime() {
    long total = 0;
    for (final long time : timeTaken) {
      total += time;
    }

    return total / timeTaken.size();
  }

  public void startTime() {
    startTime = System.nanoTime();
  }
}

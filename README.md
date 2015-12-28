Java 7 introduced a new type of <code>ExecutorService</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html" target="_blank">Java Doc</a>) called <strong>Fork/Join Framework</strong> (<a href="https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html" target="_blank">Tutorial</a>), which excels in handling recursive algorithms.  Different from other implementations of the <code>ExecutorService</code>, the Fork/Join Framework uses a work-stealing algorithm (<a href="http://gee.cs.oswego.edu/dl/papers/fj.pdf" target="_blank">Paper</a>), which maximise the threads utilisation, and provides a simpler way to deal with tasks which spawn other tasks (referred to as <em>subtasks</em>).


All code listed below is available at: <a href="https://github.com/javacreed/java-fork-join-example" target="_blank">https://github.com/javacreed/java-fork-join-example</a>.  Most of the examples will not contain the whole code and may omit fragments which are not relevant to the example being discussed.  The readers can download or view all code from the above link.


This article provides a brief description of what is referred to as traditional executor service (so to called them) and how these work.  It then introduces the Fork/Join Framework and describes how this differentiates from the traditional executor service.  The third section in this article shows a practical example of the Fork/Join Framework and demonstrates most of its main components.


<h2>Executor Service</h2>


A bank, or post office, has several counters from where the customers are served at the branches.  When a counter finishes with the current customer, the next customer in the queue will take its place and the person behind the counter, also referred to as the <em>employee</em>, starts serving the new customer.  The employee will only serve one customer at a given point in time and the customers in the queue need to wait for their turn.  Furthermore, the employees are very patient and will never ask their customers to leave or step aside, even if these are waiting for something else to happen.  The following image shows a simple view of the customers waiting and the employees serving the customers at the head of the queue.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Customers-waiting-for-their-turn.png" class="preload" rel="prettyphoto" title="Customers waiting for their turn" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Customers-waiting-for-their-turn.png" alt="Customers waiting for their turn" width="1159" height="616" class="size-full wp-image-5264" /></a>


Something similar happens in a multithreaded program where the <code>Thread</code>s (<a href="http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html" target="_blank">Java Doc</a>) represents the employees and the tasks to be carried out are the customers.  The following image is identical to the above, with just the labels updated to use the programming terminology.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Threads-and-Tasks.png" class="preload" rel="prettyphoto" title="Threads and Tasks" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Threads-and-Tasks.png" alt="Threads and Tasks" width="1176" height="619" class="size-full wp-image-5265" /></a>


This should help you relate these two aspects and better visualise the scenario being discussed.


Most of the thread pools (<a href="https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html" target="_blank">Tutorial</a>) and executor services work in this manner.  A <code>Thread</code> is assigned a <em>task</em> and will only move to the next <em>task</em> once the one at hand is finished.  Tasks can take quite a long time to finish and may block waiting for something else to happen.  This works well in many cases, but fails badly with problems that need to be solved recursively.


Let us use the same analogy that we used before with the customer waiting in the queue.  Say that <em>Customer 1</em>, who is being served by <em>Employee 1</em>, needs some information from <em>Customer 6</em>, who is not yet in the queue.  He or she (<em>Customer 1</em>) calls their friend (<em>Customer 6</em>) and waits for him or her (<em>Customer 6</em>) to come to the bank.  In the meantime, <em>Customer 1</em> stays at the counter occupying <em>Employee 1</em>.  As mentioned before, the employees are very patient and will never send a customer back to the queue or ask them to step aside until all his or her dependencies are resolved.  <em>Customer 6</em> arrives and queues as shown below.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Old-Customer-waiting-for-New-Customer.png" class="preload" rel="prettyphoto" title="Old Customer waiting for New Customer" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Old-Customer-waiting-for-New-Customer.png" alt="Old Customer waiting for New Customer" width="1163" height="601" class="size-full wp-image-5267" /></a>


With <em>Customer 1</em> still occupying an employee, and for the sake of this argument the other customers, <em>Customer 2</em> and <em>Customer 3</em>, too do the same (that is wait for something which is queued), then we have a deadlock.  All employees are occupied by customers that are waiting for something to happen.  Therefore the employees will never be free to serve the other customers.


In this example we saw a weakness of the traditional executor services when dealing with tasks, which in turn depend on other tasks created by them (referred to as subtasks).  This is very common in recursive algorithms such as Towers of Hanoi (<a href="http://en.wikipedia.org/wiki/Tower_of_Hanoi" target="_blank">Wiki</a>) or exploring a tree like data structure (calculating the total size of a directory).  The Fork/Join Framework was designed to address such problems as we will see in the following section.  Later on in this article we will also see an example of the problem discussed in this section.


<h2>Fork/Join Framework</h2>


The main weakness of the traditional executor service implementations when dealing with tasks, which in turn depend on other subtasks, is that a thread is not able to put a task back to the queue or to the side and then serves/executes an new task.  The Fork/Join Framework addresses this limitation by introducing another layer between the tasks and the threads executing them, which allows the threads to put blocked tasks on the side and deal with them when all their dependencies are executed.  In other words, if <em>Task 1</em> depends on <em>Task 6</em>, which task (<em>Task 6</em>) was created by <em>Task 1</em>, then <em>Task 1</em> is placed on the side and is only executed once <em>Task 6</em> is executed.  This frees the thread from <em>Task 1</em>, and allows it to execute other tasks, something which is not possible with the traditional executor service implementations.


This is achieved by the use of <em>fork</em> and <em>join</em> operations provided by the framework (hence the name Fork/Join).  <em>Task 1</em> forks <em>Task 6</em> and then joins it to wait for the result.  The fork operation puts <em>Task 6</em> on the queue while the join operation allows <em>Thread 1</em> to put <em>Task 1</em> on the side until <em>Task 6</em> completes.  This is how the fork/join works, fork pushes new things to the queue while the join causes the current task to be sided until it can proceed, thus blocking no threads.


The Fork/Join Framework makes use of a special kind of thread pool called <code>ForkJoinPool</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.html" target="_blank">Java Doc</a>), which differentiates it from the rest.  <code>ForkJoinPool</code> implements a work-stealing algorithm and can execute <code>ForkJoinTask</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinTask.html" target="_blank">Java Doc</a>) objects.  The <code>ForkJoinPool</code> maintains a number of threads, which number is typically based on the number of CPUs available.  Each thread has a special kind of queue, <code>Deque</code>s (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/Deque.html" target="_blank">Java Doc</a>), where all its tasks are placed.  This is quite an important point to understand.  The threads do not share a common queue, but each thread has its own queue as shown next.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Threads-and-their-Queues.png" class="preload" rel="prettyphoto" title="Threads and their Queues" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Threads-and-their-Queues.png" alt="Threads and their Queues" width="1385" height="772" class="size-full wp-image-5270" /></a>


The above image illustrates another queue that each thread has (lower part of the image).  This queue, so to call it, allows the threads to put aside tasks which are blocked waiting for something else to happen.  In other words, if the current task cannot proceed (as it performs a <em>join</em> on a subtask), then it is placed on this queue until all of its dependencies are ready.


New tasks are added to the thread's queue (using the <em>fork</em> operation) and each thread always processes the last task added to its queue.  This is quite important.  If the queue of a thread has two tasks, the last task added to the queue is processed first.  This is referred to as last in first out, LIFO (<a href="http://en.wikipedia.org/wiki/LIFO_%28computing%29" target="_blank">Wiki</a>).  


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Task1-and-Task2.png" class="preload" rel="prettyphoto" title="Task 1 and Task 2" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Task1-and-Task2.png" alt="Task 1 and Task 2" width="1385" height="772" class="size-full wp-image-5271" /></a>


In the above image, <em>Thread 1</em> has two tasks in its queue, where <em>Task 1</em> was added to the queue before <em>Task 2</em>.  Therefore, <em>Task 2</em> will be executed first by <em>Thread 1</em> and then it executes <em>Task 1</em>.   Any idle threads can take tasks from the other threads queues if available, that is, work-stealing.  A thread will always steal oldest tasks from some other thread's queue as shown in the following image.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Task-1-was-stolen-by-Thread-2.png" class="preload" rel="prettyphoto" title="Task 1 was stolen by Thread 2" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Task-1-was-stolen-by-Thread-2.png" alt="Task 1 was stolen by Thread 2" width="1385" height="772" class="size-full wp-image-5272" /></a>


As shown in the above image, <em>Thread 2</em> stole the oldest task, <em>Task 1</em>, from <em>Thread 1</em>.  As a rule of thumb, threads will always attempt to steal from their neighbouring thread to minimise the contention that may be created during the work stealing.


The order in which tasks are executed and stolen is quite important.  Ideally, work-stealing does not happen a lot as this has a cost.  When a task is moved from one thread to another, the context related with this task needs to be moved from one thread's stack to another.  The threads may be (and the Fork/Join framework spread work across all CPUs) on another CPU.  Moving thread context from one CPU to another can be even slower.  Therefore, the Fork/Join Framework minimises this as described next.


A recursive algorithm starts with a large problem and applies a divide-and-conquer technique to break down the problem into smaller parts, until these are small enough to be solved directly.  The first task added to the queue is the largest task.  The first task will break the problem into a set of smaller tasks, which tasks are added to the queue as shown next.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Tasks-and-Subtasks.png" class="preload" rel="prettyphoto" title="Tasks and Subtasks" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Tasks-and-Subtasks.png" alt="Tasks and Subtasks" width="912" height="588" class="size-full wp-image-5273" /></a>


<em>Task 1</em> represents our problem, which is divided into two tasks,  <em>Task 2</em> is small enough to solve as is, but <em>Task 3</em> needs to be divided further.  Tasks <em>Task 4</em> and <em>Task 5</em> are small enough and these require no further splitting.  This represents a typical recursive algorithm which can be split into smaller parts and then aggregates the results when ready.  A practical example of such algorithm is calculating the size of a directory.  We know that the size of a directory is equal to the size of its files.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Directories-and-Files.png" class="preload" rel="prettyphoto" title="Directories and Files" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Directories-and-Files.png" alt="Directories and Files" width="912" height="588" class="size-full wp-image-5274" /></a>


Therefore the size of <em>Dir 1</em> is equal to the size of <em>File 2</em> plus the size of <em>Dir 3</em>.  Since <em>Dir 3</em> is a directory, its size is equal to the size of its content.  In other words, the size of <em>Dir 3</em> is equal to the size of <em>File 4</em> plus the size of <em>File 5</em>.


Let us see how this is executed.  We start with one task, that is, to compute the size of directory as shown in the following image.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Step-1-Start-with-Task-1.png" class="preload" rel="prettyphoto" title="Step 1 - Start with Task 1" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Step-1-Start-with-Task-1.png" alt="Step 1 - Start with Task 1" width="1385" height="772" class="size-full wp-image-5275" /></a>


<em>Thread 1</em> will take <em>Task 1</em> which tasks forks two other subtasks.  These tasks are added to the queue of <em>Thread 1</em> as shown in the next image.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Step-2-Add-subtasks-Task-2-and-Task-3.png" class="preload" rel="prettyphoto" title="Step 2 - Add subtasks Task 2 and Task 3" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Step-2-Add-subtasks-Task-2-and-Task-3.png" alt="Step 2 - Add subtasks Task 2 and Task 3" width="1385" height="772" class="size-full wp-image-5276" /></a>


<em>Task 1</em> is waiting for the subtasks, <em>Task 2</em> and <em>Task 3</em> to finish, thus is pushed aside which frees <em>Thread 1</em>.  To use better terminology, <em>Task 1</em> joins the subtasks <em>Task 2</em> and <em>Task 3</em>.  <em>Thread 1</em> starts executing <em>Task 3</em> (the last task added to its queue), while <em>Thread 2</em> steals <em>Task 2</em>.  


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Step-3-Thread-2-steals-Task-2.png" class="preload" rel="prettyphoto" title="Step 3 - Thread 2 steals Task 2" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Step-3-Thread-2-steals-Task-2.png" alt="Step 3 - Thread 2 steals Task 2" width="1385" height="772" class="size-full wp-image-5277" /></a>


Note that <em>Thread 1</em> has already started processing its second task, while <em>Thread 3</em> is still idle.  As we will see later on, the threads will not perform the same number of work and the first thread will always produce more than the last thread.  <em>Task 3</em> forks two more subtasks which are added to the queue of the thread that is executing it.  Therefore, two more tasks are added to <em>Thread 1</em>'s queue.  <em>Thread 2</em>, ready from <em>Task 2</em>, steals again another task as shown next.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Step-4-Thread-2-steals-Task-4.png" class="preload" rel="prettyphoto" title="Step 4 - Thread 2 steals Task 4" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Step-4-Thread-2-steals-Task-4.png" alt="Step 4 - Thread 2 steals Task 4" width="1385" height="772" class="size-full wp-image-5278" /></a>


In the above example, we saw that <em>Thread 3</em> never executed a task.  This is because we only have very little subtasks.  Once <em>Task 4</em> and <em>Task 5</em> are ready, their results are used to compute <em>Task 3</em> and then <em>Task 1</em>.


As hinted before, the work is not evenly distributed among threads.  The following chart shows how the work is distributed amongst threads when calculating the size of a reasonably large directory.


<a href="http://www.javacreed.com/wp-content/uploads/2014/12/Work-Distribution.png" class="preload" rel="prettyphoto" title="Work Distribution" ><img src="http://www.javacreed.com/wp-content/uploads/2014/12/Work-Distribution.png" alt="Work Distribution" width="503" height="380" class="size-full wp-image-5279" /></a>


In the above example four threads were used.  As expected, Thread 1 performs almost 40% of the work while Thread 4 (the last thread) performs slightly more than 5% of the work.  This is another important principle to understand.  The Fork/Join Framework will not distribute work amongst threads evenly and will try to minimise the number of threads utilised.  The second threads will only take work from the first thread is this is not cooping.  As mentioned before, moving tasks between threads has a cost which the framework tries to minimise.


This section described on some detail how the Fork/Join Framework works and how threads steal work from other threads' queue.  In the following section we will see several practical example of the Fork/Join Framework and will analyse the obtained results.


<h2>Calculate Directory Total Size</h2>


To demonstrate the use of the Fork/Join Framework, we will calculate the size of a directory, which problem can be solved recursively.  The size of file can determined by the method <code>length()</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/io/File.html#length()" target="_blank">Java Doc</a>).  The size of a directory is equal to the size of all its files.  


We will use several approaches to calculate the size of a directory, some of which will use the Fork/Join Framework, and we will analyse the obtained results in each case.


<h3>Using a Single Thread (no-concurrency)</h3>


The first example will not make use of threads and simple defines the algorithm to the used.


<pre>
package com.javacreed.examples.concurrency.part1;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirSize {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirSize.class);
  
  public static long sizeOf(final File file) {
    DirSize.LOGGER.debug("Computing size of: {}", file);
    
    long size = 0;

    <span class="comments">// Ignore files which are not files and dirs</span>
    if (file.isFile()) {
      size = file.length();
    } else {
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          size += DirSize.sizeOf(child);
        }
      }
    }

    return size;
  }
}
</pre>


The class <code>DirSize </code> has a single method called <code>sizeOf()</code>, which method takes a <code>File</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/io/File.html" target="_blank">Java Doc</a>) instance as its argument.  If this instance is a file, then the method returns the file's length otherwise, if this is a directory, this method calls the method <code>sizeOf()</code> for each of the files within the directory and returns the total size.


The following example shows how to run this example, using the file path as defined by <code>FilePath.TEST_DIR</code> constant.


<pre>
package com.javacreed.examples.concurrency.part1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javacreed.examples.concurrency.utils.FilePath;

public class Example1 {

  private static final Logger LOGGER = LoggerFactory.getLogger(Example1.class);

  public static void main(final String[] args) {
    final long start = System.nanoTime();
    final long size = DirSize.sizeOf(FilePath.TEST_DIR);
    final long taken = System.nanoTime() - start;

    Example1.LOGGER.debug("Size of '{}': {} bytes (in {} nano)", FilePath.TEST_DIR, size, taken);
  }
}
</pre>


The above example will compute the size of the directory and will print all visited files before printing the total size.  The following fragment only shows the last line which is the size of the <em>downloads</em> directory under a test folder (<code>C:\Test</code>) together with the time taken to compute the size.


<pre>
...
16:55:38.045 [main] INFO Example1.java:38 - Size of 'C:\Test\': 113463195117 bytes (in 4503253988 nano) 
</pre>


To disable the logs for each file visited, simple change the log level to <code>INFO</code> (in the <code>log4j.properties</code>) and the logs will only show the final results.


<pre>
log4j.rootCategory=warn, R
log4j.logger.com.javacreed=<span class="highlight">info</span>, stdout
</pre>


Please note that the logging will only make things slower.  In fact if you run the example without logs (or the logs set to <code>INFO</code>), the size of the directory is computed much faster.


In order to obtain a more reliable result, we will run the same test several times and return the average time taken as shown next.


<pre>
package com.javacreed.examples.concurrency.part1;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javacreed.examples.concurrency.utils.FilePath;
import com.javacreed.examples.concurrency.utils.Results;

public class Example2 {

  private static final Logger LOGGER = LoggerFactory.getLogger(Example1.class);

  public static void main(final String[] args) {
    final Results results = new Results();
    for (int i = 0; i &lt; 5; i++) {
      results.startTime();
      final long size = DirSize.sizeOf(FilePath.TEST_DIR);
      final long taken = results.endTime();
      Example2.LOGGER.info("Size of '{}': {} bytes (in {} nano)", FilePath.TEST_DIR, size, taken);
    }

    final long takenInNano = results.getAverageTime();
    Example2.LOGGER.info("Average: {} nano ({} seconds)", takenInNano, TimeUnit.NANOSECONDS.toSeconds(takenInNano));
  }
}
</pre>


The same test is executed five times and the average result is printed last as shown next.


<pre>
16:58:00.496 [main] INFO Example2.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 4266090211 nano)
16:58:04.728 [main] INFO Example2.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 4228931534 nano)
16:58:08.947 [main] INFO Example2.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 4224277634 nano)
16:58:13.205 [main] INFO Example2.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 4253856753 nano)
16:58:17.439 [main] INFO Example2.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 4235732903 nano)
16:58:17.439 [main] INFO Example2.java:46 - Average: 4241777807 nano (4 seconds)
</pre>


<h3>RecursiveTask</h3>


The Fork/Join Framework provides two types of tasks, the <code>RecursiveTask</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/RecursiveTask.html" target="_blank">Java Doc</a>) and the <code>RecursiveAction</code> (<a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/RecursiveAction.html" target="_blank">Java Doc</a>).  In this section we will only talk about the <code>RecursiveTask</code>.  The <code>RecursiveAction</code> is discussed later on.


A <code>RecursiveTask</code> is a task that when executed it returns a value.  Therefore, such task returns the result of the computation.  In our case, the task returns the size of the file or directory it represents.  The class <code>DirSize</code> was modified to make use of <code>RecursiveTask</code> as shown next.


<pre>
package com.javacreed.examples.concurrency.part2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirSize {

  private static final Logger LOGGER = LoggerFactory.getLogger(DirSize.class);

  private static class SizeOfFileTask extends RecursiveTask&lt;Long&gt; {

    private static final long serialVersionUID = -196522408291343951L;

    private final File file;

    public SizeOfFileTask(final File file) {
      this.file = Objects.requireNonNull(file);
    }

    @Override
    protected Long compute() {
      DirSize.LOGGER.debug("Computing size of: {}", file);

      if (file.isFile()) {
        return file.length();
      }

      final List&lt;SizeOfFileTask&gt; tasks = new ArrayList&lt;&gt;();
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          final SizeOfFileTask task = new SizeOfFileTask(child);
          task.fork();
          tasks.add(task);
        }
      }

      long size = 0;
      for (final SizeOfFileTask task : tasks) {
        size += task.join();
      }

      return size;
    }
  }

  public static long sizeOf(final File file) {
    final ForkJoinPool pool = new ForkJoinPool();
    try {
      return pool.invoke(new SizeOfFileTask(file));
    } finally {
      pool.shutdown();
    }
  }

  private DirSize() {}

}
</pre>


Let us break this class into smaller parts and describe each separately.

<ol>
<li>
The class has a private constructor because it was not meant to be initialised.  Therefore there is no point to initialise it (creating objects of this type) and in order to prevent someone to initialising it, we made the constructor <code>private</code>.  All methods are static and these can be called against the class directly.
<pre>
  private DirSize() {}
</pre>
</li>

<li>
The method <code>sizeOf()</code> does not compute the size of the file or directory.  Instead it creates an instance of the <code>ForkJoinPool</code> and starts the computational process.  It waits for the directory size to be computed and finally it shut down the pool before exiting.

<pre>
  public static long sizeOf(final File file) {
    final ForkJoinPool pool = new ForkJoinPool();
    try {
      return pool.invoke(new SizeOfFileTask(file));
    } finally {
      pool.shutdown();
    }
  }
</pre>


The threads created by the <code>ForkJoinPool</code> are daemon threads by default.  Some articles advice against the need of shutting this pool down since these threads will not prevent the VM from shutting down.  With that said, I recommend to shut down and dispose of any objects properly when these are no longer needed.  These daemon threads may be left idle for long time even when these are not required anymore.
</li>

<li>
The method <code>sizeOf()</code> creates an instance <code>SizeOfFileTask</code>, which class extends <code>RecursiveTask&lt;Long&gt;</code>.  Therefore the invoke method will return the objects/value returned by this task.

<pre>
      return pool.invoke(new SizeOfFileTask(file));
</pre>

Note that the above code will block until the size of the directory is computed.  In other words the above code will wait for the task (and all the subtasks) to finish working before continuing.
</li>

<li>
The class <code>SizeOfFileTask</code> is an inner class within the <code>DirSize</code> class.

<pre>
  private static class SizeOfFileTask extends RecursiveTask&lt;Long&gt; {

    private static final long serialVersionUID = -196522408291343951L;

    private final File file;

    public SizeOfFileTask(final File file) {
      this.file = Objects.requireNonNull(file);
    }

    @Override
    protected Long compute() {
      <span class="comments">// Removed for brevity</span>
    }
  }
</pre>

It takes the file (which can be a directory) for which size is to be computed as argument of its sole constructor, which file cannot be <code>null</code>.  The <code>compute()</code> method is responsible from computing the work of this task.  In this case the size of the file or directory, which method is discussed next.
</li>

<li>
The <code>compute()</code> method determines whether the file passed to its constructor is a file or directory and acts accordingly.

<pre>
    @Override
    protected Long compute() {
      DirSize.LOGGER.debug("Computing size of: {}", file);

      if (file.isFile()) {
        return file.length();
      }

      final List&lt;SizeOfFileTask&gt; tasks = new ArrayList&lt;&gt;();
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          final SizeOfFileTask task = new SizeOfFileTask(child);
          task.fork();
          tasks.add(task);
        }
      }

      long size = 0;
      for (final SizeOfFileTask task : tasks) {
        size += task.join();
      }

      return size;
    }
</pre>


If the file is a file, then the method simply returns its size as shown next.


<pre>
      if (file.isFile()) {
        return file.length();
      }
</pre>


Otherwise, if the file is a directory, it lists all its sub-files and creates a new instance of <code>SizeOfFileTask</code> for each of these sub-files.


<pre>
      final List&lt;SizeOfFileTask&gt; tasks = new ArrayList&lt;&gt;();
      final File[] children = file.listFiles();
      if (children != null) {
        for (final File child : children) {
          final SizeOfFileTask task = new SizeOfFileTask(child);
          task.fork();
          tasks.add(task);
        }
      }
</pre>


For each instance of the created <code>SizeOfFileTask</code>, the <code>fork()</code> method is called.  The <code>fork()</code> method causes the new instance of <code>SizeOfFileTask</code> to be added to this thread's queue.  All created instances of <code>SizeOfFileTask</code> are saved in a list, called <code>tasks</code>.  Finally, when all tasks are forked, we need to wait for them to finish summing up their values.


<pre>
      long size = 0;
      for (final SizeOfFileTask task : tasks) {
        size += task.join();
      }

      return size;
</pre>


This is done by the <code>join()</code> method.  This <code> join()</code> will force this task to stop, step aside if needs be, and wait for the subtask to finish.  The value returned by all subtasks is added to the value of the variable <code>size</code> which is returned as the size of this directory.
</li>
</ol>


The Fork/Join Framework is more complex when compared with the simpler version which does not use multithreading.  This is a fair point, but the simpler version is 4 times slower.  The Fork/Join example took on average a second to compute the size, while the non-threading version took 4 seconds on average as shown next.


<pre>
16:59:19.557 [main] INFO Example3.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 2218013380 nano)
16:59:21.506 [main] INFO Example3.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 1939781438 nano)
16:59:23.505 [main] INFO Example3.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 2004837684 nano)
16:59:25.363 [main] INFO Example3.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 1856820890 nano)
16:59:27.149 [main] INFO Example3.java:42 - Size of 'C:\Test\': 113463195117 bytes (in 1782364124 nano)
16:59:27.149 [main] INFO Example3.java:46 - Average: 1960363503 nano (1 seconds)
</pre>


In this section we saw how multithreading help us in improving the performance of our program.  In the next section we will see the how inappropriate use of multithreading can make things worse.


<h3>ExecutorService</h3>


In the previous example we saw how concurrency improved the performance of our algorithm.  When misused, multithreading can provide poor results as we will see in this section.  We will try to solve this problem using a traditional executor service.


<strong>Please note that the code shown in this section is broken and does not work.  It will hang forever and is only included for demonstration purpose.</strong>


The class <code>DirSize</code> was modified to work with <code>ExecutorService</code> and <code>Callable</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Callable.html" target="_blank">Java Doc</a>).


<pre>
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

  private static class SizeOfFileCallable implements Callable&lt;Long&gt; {

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
        final List&lt;Future&lt;Long&gt;&gt; futures = new ArrayList&lt;&gt;();
        for (final File child : file.listFiles()) {
          futures.add(executor.submit(new SizeOfFileCallable(child, executor)));
        }

        for (final Future&lt;Long&gt; future : futures) {
          size += future.get();
        }
      }

      return size;
    }
  }

  public static &lt;T&gt; long sizeOf(final File file) {
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
</pre>


The idea is very much the same as before.  The inner class <code>SizeOfFileCallable</code> extends <code>Callable&lt;Long&gt;</code> and delegates the computation of its subtasks to the instance of <code>ExecutorService</code> passed to its constructor.  This is not required when dealing with the <code>RecursiveTask</code>, was the latter automatically adds its subclasses to the thread's queue for execution.


We will not go through this in more detail to keep this article focused on the Fork/Join Framework.  As mentioned already, this method blocks once all threads are occupied as shown next.


<pre>
17:22:39.216 [main] DEBUG DirSize.java:78 - Creating executor with 4 threads
17:22:39.222 [pool-1-thread-1] DEBUG DirSize.java:56 - Computing size of: C:\Test\
17:22:39.223 [pool-1-thread-2] DEBUG DirSize.java:56 - Computing size of: C:\Test\Dir 1
17:22:39.223 [pool-1-thread-4] DEBUG DirSize.java:56 - Computing size of: C:\Test\Dir 2
17:22:39.223 [pool-1-thread-3] DEBUG DirSize.java:56 - Computing size of: C:\Test\Dir 3
</pre>


This example is executed on a Core i5 computer, which has four available processors (as indicated by <code>Runtime.getRuntime().availableProcessors()</code> <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/Runtime.html#availableProcessors()" target="_blank">Java Doc</a>).  Once all four threads are occupied, this approach will block forever as we saw in the bank branch example in the beginning of this article.  All threads are occupied and thus cannot be used to solve the other tasks.  One can suggest using more threads.  While that may seem to be a solution, the Fork/Join Framework solved the same problem using only four threads.  Furthermore, threads are not cheap and one should not simply spawn thousands of threads just because he or she chooses an inappropriate technique.


While the phrase multithreading is overused in the programming community, the choice of multithreading technique is important as some options simply do not work in certain scenarios as we saw above.


<h3>RecursiveAction</h3>


The Fork/Join Framework supports two types of tasks.  The second type of task is the <code>RecursiveAction</code>.  These types of tasks are not meant to return anything.  These are ideal for cases where you want to do an action, such as delete a file, without returning anything.  In general you cannot delete an empty directory.  First you need to delete all its files first.  In this case the <code>RecursiveAction</code> can be used where each action either deletes the file, or first deletes all directory content and then deletes the directory itself.


Following is the final example we have in this article.  It shows the modified version of the <code>DirSize</code>, which makes use of the <code>SizeOfFileAction</code> inner class to compute the size of the directory.


<pre>
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
</pre>


This class is very similar to its predecessors.  The main difference lies in the way the final value (the size of the file or directory) is returned.  Remember that the <code>RecursiveAction</code> cannot return a value.  Instead, all tasks will share a common counter of type <code>AtomicLong</code> and these will increment this common counter instead of returning the size of the file.


Let us break this class into smaller parts and go through each part individually.  We will skip the parts that were already explained before so not to repeat ourselves.


<ol>
<li>
The method <code>sizeOf()</code> makes use of the <code>ForkJoinPool</code> as before.  The common counter, named <code>sizeAccumulator</code>, is initialised in this method too and passed to the first task.  This instance will be shared with all subtasks and all will increment this value.

<pre>
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
</pre>


Like before, this method will block until all subtasks are ready, after which returns the total size.
</li>

<li>
The inner class <code>SizeOfFileAction</code> extends <code>RecursiveAction</code> and its constructor takes two arguments. 

<pre>
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
      <span class="comments">// Removed for brevity</span>
    }
  }
</pre>

The first argument is the file (or directory) which size will be computed.  The second argument is the shared counter.
</li>

<li>
The compute method is slightly simpler here as it does not have to wait for the subtasks.  If the given file is a file, then it increments the common counter (referred to as <code>sizeAccumulator</code>).  Otherwise, if this file is a directory, it forks the new instances of <code>SizeOfFileAction</code> for each child file.

<pre>
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
</pre>


In this case the method <code>invokeAll()</code> (<a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinTask.html#invokeAll(java.util.concurrent.ForkJoinTask...)" target="_blank">Java Doc</a>) is used to fork the tasks.
</li>
</ol>


This approach takes approximately 11 seconds to complete making it the slowest of all three as shown next.


<pre>
19:04:39.925 [main] INFO Example5.java:40 - Size of 'C:\Test': 113463195117 bytes (in 11445506093 nano)
19:04:51.433 [main] INFO Example5.java:40 - Size of 'C:\Test': 113463195117 bytes (in 11504270600 nano)
19:05:02.876 [main] INFO Example5.java:40 - Size of 'C:\Test': 113463195117 bytes (in 11442215513 nano)
19:05:15.661 [main] INFO Example5.java:40 - Size of 'C:\Test': 113463195117 bytes (in 12784006599 nano)
19:05:27.089 [main] INFO Example5.java:40 - Size of 'C:\Test': 113463195117 bytes (in 11428115064 nano)
19:05:27.226 [main] INFO Example5.java:44 - Average: 11720822773 nano (11 seconds)
</pre>


This may be a surprise to many.  How is this possible, when multiple threads were used?  This is a common misconception.  Multithreading does not guarantee better performance.  In this case we have a design flaw which ideally we avoid.  The common counter named <code>sizeAccumulator</code> is shared between all threads and thus causes contention between threads.  This actually defeats the purpose of the divide and conquer technique as a bottleneck is created.


<h2>Conclusion</h2>


This article provided a detailed explanation of the Fork/Join Framework and how this can be used.  It provided a practical example and compared several approaches.  The Fork/Join Framework is ideal for recursive algorithms but it does not distribute the load amongst the threads evenly.  The tasks and subtask should not block on anything else but join and should delegate work using fork.  Avoid any blocking IO operations within tasks and minimise the mutable share state especially modifying the variable as much as possible as this has a negative effect on the overall performance.

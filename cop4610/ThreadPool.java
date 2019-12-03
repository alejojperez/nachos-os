package nachos.cop4610;

import java.util.LinkedList;

public class ThreadPool {
	/**
	 * Set of threads in the threadpool
	 */
	protected Thread threads[] = null;
	protected LinkedList<Runnable> jobs = new LinkedList<Runnable>();

	/**
	 * Initialize the number of threads required in the threadpool. 
	 * 
	 * @param size  How many threads in the thread pool.
	 */
	public ThreadPool(int size)
	{      
		threads = new Thread[size];
		
		for (int i = 0; i < size; i++) {
			threads[i] = new WorkerThread(this);
			threads[i].start();
		}
	}
	
	public int getNumWorkerThreads() {
		return threads.length;
	}
	
	public int getNumJobs() {
		return jobs.size();
	}

	/**
	 * Add a job to the queue of tasks that has to be executed. As soon as a thread is available, 
	 * it will retrieve tasks from this queue and start processing.
	 * @param r job that has to be executed asynchronously
	 * @throws InterruptedException 
	 */
	public synchronized void addToQueue(Runnable r) throws InterruptedException {
		
	     jobs.push(r);
	     this.notify();
	     
	     return; 
	}
	
	/** 
	 * Block until a job is available in the queue and retrieve the job
	 * @return A runnable task that has to be executed
	 * @throws InterruptedException 
	 */
	public synchronized Runnable getJob() throws InterruptedException {
		
		while (jobs.size() == 0) {
			this.wait();
		}
		
	    return jobs.pop();
	}
}

/**
 * The worker threads that make up the thread pool.
 */
class WorkerThread extends Thread {
	
	private ThreadPool pool = null;
	/**
	 * The constructor.
	 * 
	 * @param o the thread pool 
	 */
	WorkerThread(ThreadPool o)
	{
	     pool = o;
	}

	/**
	 * Scan for and execute tasks.
	 */
	public void run()
	{
		while (true) {
			try {
				Runnable new_job = pool.getJob();
				new_job.run();
			} catch (InterruptedException e) {
				// ignore this exception
				return;
			}
		}
		
	}
}

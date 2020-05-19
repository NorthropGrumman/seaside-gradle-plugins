/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
#ifndef __BLOCS_IThreadService_H__
#define __BLOCS_IThreadService_H__


#include <memory>



namespace blocs {

	class IThreadable;
	class Threader;

	static const size_t GLOBAL_POOL_ID = 0;

	/**
	 * Provides functionality to execute asynchronous events.  Applications should use this
	 * service in preference to creating threads themselves.
	 */
	class IThreadService {
	public:
	
		
	  /**
	   * Submits a task for asynchronous execution in a thread pool. 
	   *
	   * \param name the name of the task which may be used for logging and error reporting.
	   * \param task the task to execute.
	   * \param poolId identifies which threadPool to use.  If poolId is 0, then 
	   *        the global pool should be used.
	   */
	 virtual void submit(const std::string& name, IThreadable* task, size_t poolId = GLOBAL_POOL_ID) = 0;
	 
	  /**
	   * Submits a long running task and return the Threader that the task is running on. 
	   *
	   * \param name the name of the task which may be used for logging and error reporting.
	   * \param task the task to execute.

	   * \return a shared_ptr to a Threader
	   */
	  virtual std::shared_ptr<Threader> submitLongLivingTask(const std::string& name, IThreadable* task) = 0;

	  /**
	   * Create a specific thread pool.. 
	   *
	   * \param nThreads is the number of threads in the pool
	   * \param queueSize defines how many threadables can be waiting for threadpool to run
	   * \param deleteThreadable indicates if the threadable can be deleted after execution
	   * \return the thread pool identifier
	   */
	  virtual size_t createThreadPool(size_t nThreads, size_t queueSize = 0, bool deleteThreadable = false) = 0;

	  /**
	   * Delete a specific thread pool.
	   *
	   * \param poolId is the pool to delete
	   */	  
	  virtual void deleteThreadPool(size_t poolId, bool processQueuedRequests = false) = 0;
	};
	
}

#endif

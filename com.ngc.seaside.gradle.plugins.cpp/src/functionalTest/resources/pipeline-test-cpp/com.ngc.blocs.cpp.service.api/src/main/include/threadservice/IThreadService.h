/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
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

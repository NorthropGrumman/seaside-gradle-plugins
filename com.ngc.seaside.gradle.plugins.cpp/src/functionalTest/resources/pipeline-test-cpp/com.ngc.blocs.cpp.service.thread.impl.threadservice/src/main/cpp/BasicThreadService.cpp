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
//#include <stdexcept>

#include "threading/IThreadable.h"
#include "threading/Threader.h"
#include "threading/ThreadPool.h"

#include "BasicThreadService.h"

namespace blocs { namespace basicthreadservice {


	size_t BasicThreadService::lastPoolId = GLOBAL_POOL_ID;
	Mutex BasicThreadService::lastPoolIdGuard;

	size_t BasicThreadService::getNewPoolId() {
		ScopedLock lock(BasicThreadService::lastPoolIdGuard);
		return ++BasicThreadService::lastPoolId;
	}

   BasicThreadService::BasicThreadService() : threadPoolMapGuard(), threadPoolMap() {

   }

   BasicThreadService::~BasicThreadService() {
	   for (auto& pool: threadPoolMap) {
		   pool.second->stop();
	   }
	   threadPoolMap.clear();
   }

	void BasicThreadService::activate() {
		// Do Configuration here

		// create initial "global pool" (id GLOBAL_POOL_ID) here
		size_t concurrentThreadsSupported = std::thread::hardware_concurrency();
		std::shared_ptr<ThreadPool> p(new ThreadPool(concurrentThreadsSupported == 0 ? 2 : concurrentThreadsSupported));
		threadPoolMap[GLOBAL_POOL_ID] = p;

	}

	void BasicThreadService::start() {

	}

	void BasicThreadService::stop() {

	}

	void BasicThreadService::deactivate() {

	}

	void BasicThreadService::submit(const std::string& name, IThreadable* task, size_t poolId) {

		static long peakQ = 0;

		ScopedLock lock(threadPoolMapGuard);
		auto p_iter = threadPoolMap.find(poolId);

		// if not found, then log it and use the global pool
		if (p_iter == threadPoolMap.end()) {
			// LOGBLOCS_WARN(logService, loggerId, message.c_str());
			std::cout << "Unable to submit to ThreadPool in BasicThreadService::submit with id: " << poolId << std::endl;
			p_iter = threadPoolMap.find(GLOBAL_POOL_ID);
		}

		std::vector<long> invokeState = (*p_iter).second->invoke(task, name);

		if (invokeState[3] > peakQ) peakQ = invokeState[3];
		/*
		if (LOGBLOCS_TRACE_ENABLED(logService, loggerId) {
			std::ostringstream oss;
			oss << timeService->getSystemClockUniversalTime() << " "
				<< timeService->getCurrentTime() << " " <<
				<< "Submitted_task [" << name << "] poolId= [" << poolId << "]  poolThreads= " <<
				<< invokeState[0] << " "
				<< invokeState[1] << " "
				<< invokeState[2] << " "
				<< "poolqueue = " << invokeState[3] << "/"
				<< peakQ << " "
				<< invokeState[4] << std::endl;
			LOGBLOCS_RAW(logServicom.ngc.blocs.cpp.service.thread.impl.threadservice/build/install/mainTest/linux_x86_64/mainTestce, loggerId, oss.str());
		}
	    */

		// Check for thread pool overflow
		if (invokeState[5] == 1) {
			//LOGBLOCS_ERROR(logService, loggerId, "FAILED TO EXECUTE TASK DUE TO THREAD POOL OVERFLOW: %s - %d", name, poolId);
			std::cout << "FAILED TO EXECUTE TASK DUE TO THREAD POOL OVERFLOW: " << name << "-" << poolId << std::endl;
		}
	}

	std::shared_ptr<Threader> BasicThreadService::submitLongLivingTask(const std::string& name, IThreadable* task) {
		std::shared_ptr<Threader> threader(new Threader(name));
		threader->execute(task);
		return threader;
	}

	size_t BasicThreadService::createThreadPool(size_t nThreads, size_t queueSize, bool deleteThreadable) {
		// set maxThreads = nThreads, queueSize = 0, deleteThreadable = false
		// setting queueSize = 0 will default the queueSize to nThreads*2
		std::shared_ptr<ThreadPool> p(new ThreadPool(nThreads, queueSize, deleteThreadable));
		size_t p_id = BasicThreadService::getNewPoolId();

		ScopedLock lock(threadPoolMapGuard);
		threadPoolMap[p_id] = p;
		return p_id;
	}

	void BasicThreadService::deleteThreadPool(size_t poolId, bool processQueuedRequests) {
		ScopedLock lock(threadPoolMapGuard);
		auto p_iter = threadPoolMap.find(poolId);

		// if not found, then log it and return (for now, just use cout)
		if (p_iter == threadPoolMap.end()) {
			// LOGBLOCS_warn(logService, loggerId, message.c_str());
			std::cout << "Unable to delete ThreadPool in BasicThreadService::deleteThreadPool with id: " << poolId << std::endl;
			return;
		}

		// stop the threads
		try {
			(*p_iter).second->stop(processQueuedRequests);
		}
		catch (...) {
			std::cerr << "BasicThreadService::deleteThreadPool " << poolId << " exception occurred." << std::endl;
		}

		// remove the thread pool
		threadPoolMap.erase(p_iter);
	}



}}

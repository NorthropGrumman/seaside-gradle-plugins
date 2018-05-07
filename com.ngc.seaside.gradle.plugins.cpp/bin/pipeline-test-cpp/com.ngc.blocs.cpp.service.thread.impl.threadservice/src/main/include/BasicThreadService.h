/**
 *
 *  Northrop Grumman Proprietary
 *  ____________________________
 *
 *   Copyright (C) 2017, Northrop Grumman Systems Corporation
 *   All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains the property of
 *  Northrop Grumman Systems Corporation. The intellectual and technical concepts
 *  contained herein are proprietary to Northrop Grumman Systems Corporation and
 *  may be covered by U.S. and Foreign Patents or patents in process, and are
 *  protected by trade secret or copyright law. Dissemination of this information
 *  or reproduction of this material is strictly forbidden unless prior written
 *  permission is obtained from Northrop Grumman.
 */
#ifndef __BLOCS_BasicThreadService_H__
#define __BLOCS_BasicThreadService_H__

#include <map>
#include <memory>

#include "threadservice/IThreadService.h"
#include "threading/Mutex.h"

namespace blocs {


	class IThreadable;
	class Threader;
	class ThreadPool;

	namespace basicthreadservice {


		/**
		 * Implements the IThreadService interface.
		 */
		class BasicThreadService : public IThreadService {
		public:

			BasicThreadService();
			virtual ~BasicThreadService();

			void activate();
			void start();
			void stop();
			void deactivate();


			void submit(const std::string& name, IThreadable* task, size_t poolId = GLOBAL_POOL_ID);

			std::shared_ptr<Threader> submitLongLivingTask(const std::string& name, IThreadable* task);

			size_t createThreadPool(size_t nThreads, size_t queueSize = 0, bool deleteThreadable = false);

			void deleteThreadPool(size_t poolId, bool processQueuedRequests = false);

		private:
			static size_t lastPoolId;
			static Mutex lastPoolIdGuard;

			Mutex threadPoolMapGuard;
			std::map<size_t, std::shared_ptr<ThreadPool>> threadPoolMap;

			static size_t getNewPoolId();

		};
	}
}

#endif

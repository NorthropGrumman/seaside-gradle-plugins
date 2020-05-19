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

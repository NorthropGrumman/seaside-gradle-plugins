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
#ifndef _BLOCS_ISyncEventService_H
#define _BLOCS_ISyncEventService_H

#include "event/IEventService.h"
#include "event/IEvent.h"
#include "event/IEventListener.h"
#include "event/IEventTopic.h"
#include "threading/Mutex.h"

#include <map>
#include <vector>

namespace blocs { namespace synceventservice {


	class SyncEventService : public IEventService {
	private :
		class IEventTopicCompareLess : std::binary_function<std::shared_ptr<const IEventTopic>, std::shared_ptr<const IEventTopic>, bool> {
		public:
			bool  operator() (std::shared_ptr<const IEventTopic> a, std::shared_ptr<const IEventTopic> b) const {
				return (*a).getName() < (*b).getName();
			}
		};

		typedef std::map<std::shared_ptr<const IEventTopic>, std::vector<std::shared_ptr<IEventListener> >, IEventTopicCompareLess> TopicListenersMap;
		TopicListenersMap topicListenersMap;
		Mutex listenersGuard;

	public :
		SyncEventService();

		void activate();
		void start();
		void stop();
		void deactivate();

		//@Override
		void publish(std::shared_ptr<IEvent> event);

		//@Override
		void addListener(
			std::shared_ptr<const IEventTopic> topic,
			std::shared_ptr<IEventListener> listener);

		//@Override
		void removeListener(
			std::shared_ptr<const IEventTopic> topic,
			std::shared_ptr<IEventListener> listener);

	};

}}

#endif

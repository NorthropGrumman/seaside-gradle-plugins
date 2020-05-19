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
#include "SyncEventService.h"
#include "event/IEventListener.h"


namespace blocs { namespace synceventservice {

		SyncEventService::SyncEventService() {
		}

		void SyncEventService::activate() {
		}

		void SyncEventService::start() {
		}

		void SyncEventService::stop() {
		}

		void SyncEventService::deactivate() {
			ScopedLock lock(listenersGuard);
			topicListenersMap.clear();
		}

		void SyncEventService::publish(std::shared_ptr<IEvent> event) {

			ScopedLock lock(listenersGuard);
			TopicListenersMap::iterator topicListenersI = topicListenersMap.find(event->getTopic());
			if (topicListenersI != topicListenersMap.end()) {
				for (	std::vector<std::shared_ptr<IEventListener> >::const_iterator listenersI = topicListenersI->second.begin();
						listenersI != topicListenersI->second.end();
						++listenersI) {
					(*listenersI)->eventReceived(*event);

				}
			}
		}

		//@Override
		void SyncEventService::addListener(
				std::shared_ptr<const IEventTopic> topic,
				std::shared_ptr<IEventListener> listener) {

			ScopedLock lock(listenersGuard);
			TopicListenersMap::iterator topicListenersI = topicListenersMap.find(topic);
			if (topicListenersI != topicListenersMap.end()) {
				topicListenersI->second.push_back(listener);
			}
			else {
				std::vector<std::shared_ptr<IEventListener> > listenersVector;
				listenersVector.push_back(listener);
				topicListenersMap.insert(std::make_pair(topic, listenersVector));
			}
		}

		//@Override
		void SyncEventService::removeListener(
				std::shared_ptr<const IEventTopic> topic,
				std::shared_ptr<IEventListener> listener) {

			ScopedLock lock(listenersGuard);
			TopicListenersMap::iterator topicListenersI = topicListenersMap.find(topic);
			if (topicListenersI != topicListenersMap.end()) {
				for (std::vector<std::shared_ptr<IEventListener> >::iterator listenersI = topicListenersI->second.begin();
					listenersI != topicListenersI->second.end();
					++listenersI) {
					if ((*listenersI) == listener) {
						topicListenersI->second.erase(listenersI);
						break;
					}

				}
			}
		}

} }

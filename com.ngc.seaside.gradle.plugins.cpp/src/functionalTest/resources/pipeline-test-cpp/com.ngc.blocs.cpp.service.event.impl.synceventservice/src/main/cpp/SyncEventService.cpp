/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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

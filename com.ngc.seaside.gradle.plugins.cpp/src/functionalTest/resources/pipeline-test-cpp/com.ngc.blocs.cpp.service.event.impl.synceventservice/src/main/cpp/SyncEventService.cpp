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

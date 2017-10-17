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

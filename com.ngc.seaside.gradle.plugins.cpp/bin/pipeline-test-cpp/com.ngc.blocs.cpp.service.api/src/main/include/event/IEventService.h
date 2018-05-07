#ifndef _BLOCS_IEventService_H
#define _BLOCS_IEventService_H

#include "event/IEvent.h"
#include "event/IEventListener.h"
#include "event/IEventTopic.h"
#include <memory>

namespace blocs {

	class IEventService {

	public:
		virtual void publish(std::shared_ptr<IEvent> event) = 0;

		virtual void addListener(
			std::shared_ptr<const IEventTopic> topic,
			std::shared_ptr<IEventListener> listener) = 0;

		virtual void removeListener(
			std::shared_ptr<const IEventTopic> topic,
			std::shared_ptr<IEventListener> listener) = 0;
	};

}

#endif

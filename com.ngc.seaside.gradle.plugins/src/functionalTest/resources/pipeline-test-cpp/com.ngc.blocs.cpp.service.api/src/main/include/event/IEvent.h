#ifndef _BLOCS_IEvent_H
#define _BLOCS_IEvent_H

#include "event/IEventTopic.h"
#include <memory>

namespace blocs {


	class IEvent {
	public:
		virtual ~IEvent() = default;

		virtual std::shared_ptr<const IEventTopic> getTopic() const = 0;
	};

	template <typename T>
	class IEventT : public IEvent {
	public:

		virtual const T & getSource() const = 0;

		virtual std::shared_ptr<const IEventTopicT<T> > getTypedTopic() const = 0;
	};
}

#endif

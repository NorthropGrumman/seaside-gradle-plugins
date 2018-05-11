#ifndef _BLOCS_DefaultEvent_H
#define _BLOCS_DefaultEvent_H

#include "event/IEvent.h"
#include "event/IEventTopic.h"
#include <memory>
#include <sstream>

namespace blocs {

	template <typename T>
	class DefaultEvent : public IEventT<T> {

	private :
		std::shared_ptr<const T> source;
		std::shared_ptr<const IEventTopicT<T> > topic;

	public:

		DefaultEvent* setSource(std::shared_ptr<const T> _source) {
			source = _source;
			return this;
		}

		DefaultEvent* setTopic(std::shared_ptr<const IEventTopicT<T> > _topic) {
			topic = _topic;
			return this;
		}

		const T & getSource() const {
			return *source;
		}

		std::shared_ptr<const IEventTopic> getTopic() const {
			return std::dynamic_pointer_cast<const IEventTopic>(topic);
		}

		std::shared_ptr<const IEventTopicT<T> > getTypedTopic() const {
			return topic;
		}

		
		const std::string toString(void) const {
			std::ostringstream oss;
			if (topic != NULL) {
				oss << "topic: " << getTopic()->getName();
			}
			else {
				oss << "topic: NULL";
			}

			if (source != NULL) {
				oss << " content: " << getSource();
			}
			else {
				oss << " content: NULL";
			}
			return oss.str();
		}

	};
}

#endif

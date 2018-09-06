/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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

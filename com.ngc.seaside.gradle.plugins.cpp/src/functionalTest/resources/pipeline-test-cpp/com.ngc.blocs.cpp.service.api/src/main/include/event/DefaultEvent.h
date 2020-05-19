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

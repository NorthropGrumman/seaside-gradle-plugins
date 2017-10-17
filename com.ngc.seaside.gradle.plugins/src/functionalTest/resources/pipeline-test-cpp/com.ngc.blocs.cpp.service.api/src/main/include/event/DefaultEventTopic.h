#ifndef _BLOCS_DefaultEventTopic_H
#define _BLOCS_DefaultEventTopic_H

#include "event/IEventTopic.h"

namespace blocs {

	template <typename T>
	class DefaultEventTopic : public IEventTopicT<T> {

	private:
		std::string name;
		//T type;

	public:

		DefaultEventTopic& setName(const std::string & _name) {
			name = _name;
			return *this;
		}

		//DefaultEventTopic setType(T type) {
		//	this.type = type;
		//	return this;
		//}

		std::string getName() const {
			return name;
		}

		//T getType() {
		//	return type;
		//}

	};
}

#endif

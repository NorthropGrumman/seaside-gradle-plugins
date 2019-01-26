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

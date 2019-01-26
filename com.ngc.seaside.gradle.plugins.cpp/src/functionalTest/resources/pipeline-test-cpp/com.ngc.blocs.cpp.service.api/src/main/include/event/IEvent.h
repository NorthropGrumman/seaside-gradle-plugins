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

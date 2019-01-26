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

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

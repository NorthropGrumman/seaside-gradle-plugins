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
#ifndef _BLOCS_IEventTopic_H
#define _BLOCS_IEventTopic_H

#include <string>

namespace blocs {

	class IEventTopic {
	public :
		virtual ~IEventTopic() = default;
		virtual std::string getName() const = 0;
	};

	template <typename T>
	class IEventTopicT : public IEventTopic {

	public :

		//typedef T Type;

		//virtual std::string getName() const = 0;

		//virtual T getType() = 0;
	};

}

#endif

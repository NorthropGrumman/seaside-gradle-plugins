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

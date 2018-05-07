#ifndef _BLOCS_IEventListener_H
#define _BLOCS_IEventListener_H

#include "event/IEvent.h"

namespace blocs {

	class IEventListener {
	public :
		virtual ~IEventListener() = default;

		virtual void eventReceived(const IEvent & event) = 0;
	};

	template <typename T>
	class IEventListenerT : public IEventListener {

	public:

		//virtual void eventReceived(const IEventT<T> & event) = 0;

	};
}

#endif

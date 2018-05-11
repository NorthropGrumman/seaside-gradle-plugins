#ifndef LogServiceActivator_H_
#define LogServiceActivator_H_

#include "celix/dm/DmActivator.h"

using namespace celix::dm;

class LogServiceActivator : public DmActivator {
private:

public:
   LogServiceActivator(DependencyManager& mng) : DmActivator {mng} {}
   virtual void init();
};

#endif //LogServiceActivator_H_

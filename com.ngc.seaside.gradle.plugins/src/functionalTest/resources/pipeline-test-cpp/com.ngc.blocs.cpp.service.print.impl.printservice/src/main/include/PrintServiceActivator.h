#ifndef PRINTSERVICEACTIVATOR_H_
#define PRINTSERVICEACTIVATOR_H_

#include "celix/dm/DmActivator.h"

using namespace celix::dm;

class PrintServiceActivator : public DmActivator {
private:
    const std::string word {"C++ World"};
public:
    PrintServiceActivator(DependencyManager& mng) : DmActivator {mng} {}
    virtual void init();
};

#endif //PRINTSERVICEACTIVATOR_H_

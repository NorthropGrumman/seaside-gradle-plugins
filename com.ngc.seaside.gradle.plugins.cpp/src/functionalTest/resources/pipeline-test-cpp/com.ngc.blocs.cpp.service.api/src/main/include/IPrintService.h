#ifndef IPRINTSERVICE_H
#define IPRINTSERVICE_H

#define IPRINTSERVICE_VERSION "1.0.0"
#define IPRINTSERVICE_CONSUMER_RANGE "[1.0.0,2.0.0)"

#include <iostream>

class IPrintService {
protected:
    IPrintService() = default;
    virtual ~IPrintService() = default;
public:
    virtual void print(
    		const std::string& className,
    		const std::string& value) = 0;
};

#endif //IPRINTSERVICE_H

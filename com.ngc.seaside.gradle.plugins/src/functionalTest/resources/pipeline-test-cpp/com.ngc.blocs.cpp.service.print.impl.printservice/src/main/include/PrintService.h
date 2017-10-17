#ifndef PRINTSERVICE_H
#define PRINTSERVICE_H

#include <iostream>
#include "IPrintService.h"

class PrintService : public IPrintService {

public:
    PrintService() = default;
    virtual ~PrintService() = default;

    void activate();
    void start();
    void stop();
    void deactivate();

    virtual void print(const std::string& className, const std::string& input);
};

#endif //PRINTSERVICE_H

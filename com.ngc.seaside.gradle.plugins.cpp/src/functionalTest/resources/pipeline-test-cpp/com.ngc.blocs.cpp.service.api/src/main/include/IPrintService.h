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

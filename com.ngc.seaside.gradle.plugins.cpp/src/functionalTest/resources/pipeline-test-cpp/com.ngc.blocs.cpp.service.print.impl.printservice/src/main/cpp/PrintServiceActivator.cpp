/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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
#include "PrintService.h"
#include "PrintServiceActivator.h"

using namespace celix::dm;

DmActivator* DmActivator::create(DependencyManager& mng) {
    return new PrintServiceActivator(mng);
}

void PrintServiceActivator::init() {
    std::shared_ptr<PrintService> printService = std::shared_ptr<PrintService>{new PrintService{}};

    Properties props;
    props["meta.info.key"] = "meta.info.value";

    // using a pointer a instance. Also supported is lazy initialization
    // (default constructor needed) or a rvalue reference (move)
    createComponent(printService)
        .addInterface<IPrintService>(IPRINTSERVICE_VERSION, props)
        .setCallbacks(
        		&PrintService::activate,
        		&PrintService::start,
				&PrintService::stop,
				&PrintService::deactivate);
}

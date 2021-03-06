/**
 * UNCLASSIFIED
 *
 * Copyright 2020 Northrop Grumman Systems Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

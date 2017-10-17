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

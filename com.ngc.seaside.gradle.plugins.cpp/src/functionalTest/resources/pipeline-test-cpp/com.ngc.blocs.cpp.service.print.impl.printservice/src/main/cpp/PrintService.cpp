#include "PrintService.h"
#include <iostream>

void PrintService::activate() {
    print("PrintService", "activate");
}

void PrintService::start() {
	print("PrintService", "start");
}

void PrintService::stop() {
	print("PrintService", "stop");
}

void PrintService::deactivate() {
	print("PrintService", "deactivate");
}

void PrintService::print(
		const std::string& className,
		const std::string& input) {
	std::cout << className << " : " << input << std::endl;
}

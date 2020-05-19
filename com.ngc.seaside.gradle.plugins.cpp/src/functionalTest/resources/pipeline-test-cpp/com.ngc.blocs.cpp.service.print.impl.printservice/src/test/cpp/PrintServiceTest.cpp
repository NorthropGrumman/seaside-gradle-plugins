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
#include <fstream>
#include <sstream>
#include <memory>

#include "gtest/gtest.h"

#include "PrintService.h"

class PintServiceTestFixture : public ::testing::Test {
protected:
	std::shared_ptr<PrintService> printService;

	virtual void SetUp() {
		printService = std::shared_ptr<PrintService>(new PrintService());
		printService->activate();
	}

	virtual void TearDown() {

	}

};


TEST_F(PintServiceTestFixture, testLogError) {

  printService->print("", "");

//	logService->setLoggingLevel(loggerId, LogLevel::LOG_ERROR);
//
//	logStream.str("");	// reset the logstream
//	logService->logError(LOGFROM, loggerId, testMessage.c_str());
//	std::string result1 = logStream.str();
//	logStream.str("");
//
//	logService->logError(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
//	std::string result2 = logStream.str();
//	logStream.str("");
//
//	logService->logError("testLogError", loggerId, "Testing location field");
//	std::string result3 = logStream.str();
//	logStream.str("");


//    EXPECT_TRUE(result1.length() != 0);
//    EXPECT_TRUE(result1.find("ERROR") != std::string::npos);
//    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);
//
//    EXPECT_TRUE(result2.length() > 0);
//    EXPECT_TRUE(result2.find("ERROR") != std::string::npos);
//    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);
//
//    EXPECT_TRUE(result3.length() > 0);
//    EXPECT_TRUE(result3.find("ERROR") != std::string::npos);
//    EXPECT_TRUE(result3.find("testLogError") != std::string::npos);


}
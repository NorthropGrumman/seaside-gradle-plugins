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
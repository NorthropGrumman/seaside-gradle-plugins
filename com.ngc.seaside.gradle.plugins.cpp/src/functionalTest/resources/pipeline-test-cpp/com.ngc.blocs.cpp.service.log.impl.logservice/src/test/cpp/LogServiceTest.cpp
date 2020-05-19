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

#include "LogService.h"
#include "logger/OstreamOutputStrategy.h"
#include "logger/TextFileOutputStrategy.h"

using namespace blocs::basiclogservice;

class LogServiceTestFixture : public ::testing::Test {
protected:
	std::shared_ptr<LogService> logService;
	std::string loggerId;
	std::ostringstream logStream;

	std::string testMessage;
	std::string testParam1;
	std::string testParam2;
	std::string testFormat;
	std::string expResultStr;

	virtual void SetUp() {
		logStream.str("");
		loggerId = "myLogger";
		testMessage = "LOGtestMESSAGE";
		testParam1 = "PARAM1";
		testParam2 = "PARAM2";
		testFormat = "%s%s";
		expResultStr = testParam1 + testParam2;

		logService = std::shared_ptr<LogService>(new LogService());
		logService->activate();

		logService->createLogger(loggerId);

		//std::unique_ptr<OstreamOutputStrategy> strategy(new OstreamOutputStrategy(logStream));
		logService->addLogOutputStrategy(loggerId,new OstreamOutputStrategy(logStream)); // logService now has ownership

	}

	virtual void TearDown() {
		logService->removeLogger(loggerId);
	}

};


TEST_F(LogServiceTestFixture, testLogError) {

	logService->setLoggingLevel(loggerId, LogLevel::LOG_ERROR);

	logStream.str("");	// reset the logstream
	logService->logError(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logError(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logError("testLogError", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");


    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("ERROR") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("ERROR") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("ERROR") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogError") != std::string::npos);


}


TEST_F(LogServiceTestFixture, testLogErrorMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::LOG_ERROR);

	logStream.str("");	// reset the logstream
	LOGBLOCS_ERROR(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_ERROR(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_ERROR_ENABLED(logService, loggerId);

    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("ERROR") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("ERROR") != std::string::npos);
    EXPECT_TRUE(result2.find(testParam1 + testParam2) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogWarn) {
	logService->setLoggingLevel(loggerId, LogLevel::WARN);

	logStream.str("");	// reset the logstream

	logService->logWarn(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logWarn(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logWarn("testLogWarn", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");

	// set the log level to ERROR and verify that nothing is logged
	logService->setLoggingLevel(loggerId, LogLevel::LOG_ERROR);
	logService->logWarn(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result4 = logStream.str();
	logStream.str("");

    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("WARN"));
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("WARN"));
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("WARN") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogWarn") != std::string::npos);

    EXPECT_TRUE(result4.length() == 0);
}


TEST_F(LogServiceTestFixture, testLogWarnMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::WARN);

	logStream.str("");	// reset the logstream
	LOGBLOCS_WARN(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_WARN(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_WARN_ENABLED(logService, loggerId);

    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("WARN") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("WARN") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogInfo) {

	logService->setLoggingLevel(loggerId, LogLevel::INFO);

	logStream.str("");	// reset the logstream
	logService->logInfo(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logInfo(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logInfo("testLogInfo", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");


	// set the log level to WARN and verify that nothing is logged
	logService->setLoggingLevel(loggerId, LogLevel::WARN);
	logService->logInfo(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result4 = logStream.str();
	logStream.str("");

    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("INFO"));
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("INFO") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("INFO") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogInfo") != std::string::npos);

    EXPECT_TRUE(result4.length() == 0);
}



TEST_F(LogServiceTestFixture, testLogInfoMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::INFO);

	logStream.str("");	// reset the logstream
	LOGBLOCS_INFO(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_INFO(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_INFO_ENABLED(logService, loggerId);


    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("INFO") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("INFO") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogDebug) {

	logService->setLoggingLevel(loggerId, LogLevel::DEBUG_REGULAR);

	logStream.str("");	// reset the logstream
	logService->logDebug(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logDebug(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logDebug("testLogInfo", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");

	// set the log level to INFO and verify that nothing is logged
	logService->setLoggingLevel(loggerId, LogLevel::INFO);
	logService->logDebug(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result4 = logStream.str();
	logStream.str("");


    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("DEBUG") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("DEBUG") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("DEBUG") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogInfo") != std::string::npos);

    EXPECT_TRUE(result4.length() == 0);

}


TEST_F(LogServiceTestFixture, testLogDebugMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::DEBUG_REGULAR);

	logStream.str("");	// reset the logstream
	LOGBLOCS_DEBUG(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_DEBUG(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_DEBUG_ENABLED(logService, loggerId);

    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("DEBUG") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("DEBUG") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogFatal) {

	logService->setLoggingLevel(loggerId, LogLevel::FATAL);

	logStream.str("");	// reset the logstream
	logService->logFatal(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logFatal(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logFatal("testLogInfo", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");

	// set the log level to NONE and verify that nothing is logged
	logService->setLoggingLevel(loggerId, LogLevel::NONE);
	logService->logFatal(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result4 = logStream.str();
	logStream.str("");


    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("FATAL") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("FATAL") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("FATAL") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogInfo") != std::string::npos);

    EXPECT_TRUE(result4.length() == 0);

}


TEST_F(LogServiceTestFixture, testLogFatalMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::FATAL);

	logStream.str("");	// reset the logstream
	LOGBLOCS_FATAL(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_FATAL(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_FATAL_ENABLED(logService, loggerId);

    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("FATAL") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("FATAL") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogUsergram) {

	logService->setLoggingLevel(loggerId, LogLevel::USERGRAM);

	logStream.str("");	// reset the logstream
	logService->logUsergram(LOGFROM, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logUsergram(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logUsergram("testLogInfo", loggerId, "Testing location field");
	std::string result3 = logStream.str();
	logStream.str("");

	// set the log level to NONE and verify that nothing is logged
	logService->setLoggingLevel(loggerId, LogLevel::NONE);
	logService->logFatal(LOGFROM, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result4 = logStream.str();
	logStream.str("");


    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("USERGRAM") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("USERGRAM") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("USERGRAM") != std::string::npos);
    EXPECT_TRUE(result3.find("testLogInfo") != std::string::npos);

    EXPECT_TRUE(result4.length() == 0);

}


TEST_F(LogServiceTestFixture, testLogUsergramMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::USERGRAM);

	logStream.str("");	// reset the logstream
	LOGBLOCS_USERGRAM(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_USERGRAM(logService, loggerId, testFormat.c_str(), testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_USERGRAM_ENABLED(logService, loggerId);


    EXPECT_TRUE(result1.length() != 0);
    EXPECT_TRUE(result1.find("USERGRAM") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("USERGRAM") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);

    EXPECT_TRUE(isEnabled);

}


TEST_F(LogServiceTestFixture, testLogRaw) {

	logService->setLoggingLevel(loggerId, LogLevel::ALL);

	logStream.str("");	// reset the logstream
	logService->logRaw(loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	logService->logRaw(loggerId, "TestParams: %s%s", testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	logService->logRaw(loggerId, "Testing No Location Field");
	std::string result3 = logStream.str();
	logStream.str("");


    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("LOGtestMESSAGE") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);
    EXPECT_TRUE(result1.find("RAW") == std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("TestParams") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);
    EXPECT_TRUE(result2.find("RAW") == std::string::npos);

    EXPECT_TRUE(result3.length() > 0);
    EXPECT_TRUE(result3.find("Testing") != std::string::npos);
    EXPECT_TRUE(result3.find("No") != std::string::npos);
    EXPECT_TRUE(result3.find("Location") != std::string::npos);
    EXPECT_TRUE(result3.find("Field") != std::string::npos);

}


TEST_F(LogServiceTestFixture, testLogRawMacros) {

	logService->setLoggingLevel(loggerId, LogLevel::ALL);

	logStream.str("");	// reset the logstream
	LOGBLOCS_RAW(logService, loggerId, testMessage.c_str());
	std::string result1 = logStream.str();
	logStream.str("");

	LOGBLOCS_RAW(logService, loggerId, "TestParams: %s%s", testParam1.c_str(), testParam2.c_str());
	std::string result2 = logStream.str();
	logStream.str("");

	bool isEnabled = LOGBLOCS_RAW_ENABLED(logService, loggerId);


    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("LOGtestMESSAGE") != std::string::npos);
    EXPECT_TRUE(result1.find(testMessage) != std::string::npos);
    EXPECT_TRUE(result1.find("RAW") == std::string::npos);

    EXPECT_TRUE(result2.length() > 0);
    EXPECT_TRUE(result2.find("TestParams") != std::string::npos);
    EXPECT_TRUE(result2.find(expResultStr) != std::string::npos);
    EXPECT_TRUE(result1.find("RAW") == std::string::npos);

    EXPECT_TRUE(isEnabled);

}



TEST_F(LogServiceTestFixture, testTextFileOutputStrategy) {

	logService->setLoggingLevel(loggerId, LogLevel::ALL);

	std::string filename("LogServiceTest.log");

	logService->addLogOutputStrategy(loggerId, new TextFileOutputStrategy(filename, false)); // logService now has ownership

	// write something to the file
	logService->logInfo(LOGFROM, loggerId, "This is a log string to a file");
	std::string result1 = logStream.str();
	logStream.str("");

	// Check the ostringstream
    EXPECT_TRUE(result1.length() > 0);
    EXPECT_TRUE(result1.find("file") != std::string::npos);

    // remove the logger so we can read the file
	logService->removeLogger(loggerId);

	// verify file exists from the TextFileOutputStrategy
	std::ifstream infile(filename);
	EXPECT_TRUE(infile.good());

    // read the string in from the file
    std::string result2;
    std::getline(infile, result2);
    EXPECT_TRUE(result2.find("INFO") != std::string::npos);
    EXPECT_TRUE(result2.find("file") != std::string::npos);
    EXPECT_TRUE(result2.find("LogServiceTest.cpp") != std::string::npos);

}



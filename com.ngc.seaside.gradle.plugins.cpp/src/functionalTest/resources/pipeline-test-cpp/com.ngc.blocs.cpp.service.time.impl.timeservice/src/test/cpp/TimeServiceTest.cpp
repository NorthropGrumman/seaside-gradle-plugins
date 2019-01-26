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
#include <memory>
//#include <thread>

#include "gtest/gtest.h"

#include "TimeService.h"


#include <iostream>
using namespace std;

using namespace blocs::realworldtimeservice;

class TimeServiceTestFixture : public ::testing::Test {
protected:
	std::shared_ptr<TimeService> timeService;
	virtual void SetUp() {
		timeService = std::shared_ptr<TimeService>(new TimeService());
		timeService->activate();
	}
	virtual void TearDown() {}
};

TEST_F(TimeServiceTestFixture, testConsecutiveClocks) {
	blocs::Time time0 = timeService->getApplicationClockTime();
   cout << time0.getFormattedTimeString() << endl;
	blocs::Time time1 = timeService->getApplicationClockTime();
   cout << time1.getFormattedTimeString() << endl;

   //EXPECT_TRUE(time0.getTimePoint().time_since_epoch().count() < time1.getTimePoint().time_since_epoch().count());
   EXPECT_TRUE(time0 < time1);
}

TEST_F(TimeServiceTestFixture, testConsecutiveElapsed) {
   blocs::Duration time0 = timeService->getElapsedApplicationTimeSinceEpoch();
   cout << time0.asMicroseconds() << endl;
   //std::this_thread::sleep_for (std::chrono::seconds(1));
   blocs::Duration time1 = timeService->getElapsedApplicationTimeSinceEpoch();
   cout << time1.asMicroseconds() << endl;

   //EXPECT_TRUE(time0.getTimePoint().time_since_epoch().count() < time1.getTimePoint().time_since_epoch().count());
   EXPECT_TRUE(time0 < time1);
}

//TODO Why does this print out nothing?
TEST_F(TimeServiceTestFixture, testGetApplicationClockEpoch) {
	blocs::Time time0 = timeService->getApplicationClockEpoch();
	cout << "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " << endl;

	cout << time0.getFormattedTimeString() << endl;
	cout << "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " << endl;
	EXPECT_FALSE(time0.getFormattedTimeString().empty());
}

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

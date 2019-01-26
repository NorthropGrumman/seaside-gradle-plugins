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
#include "gtest/gtest.h"

#include "time/Time.h"

using namespace blocs;

class TimeTestFixture : public ::testing::Test {
public:
	virtual void SetUp() {}
	virtual void TearDown() {}
};

TEST_F(TimeTestFixture, testEqualsOperator) {
	blocs::Time time0 = blocs::Time();
	blocs::Time time1 = blocs::Time(time0);
	EXPECT_TRUE(time0 == time1);
}


TEST_F(TimeTestFixture, testGetTimePoint) {
	blocs::Time time0 = blocs::Time();
	blocs::Time::TimePointType timePoint = time0.getTimePoint();
	EXPECT_TRUE(timePoint.time_since_epoch().count() > 0);
}


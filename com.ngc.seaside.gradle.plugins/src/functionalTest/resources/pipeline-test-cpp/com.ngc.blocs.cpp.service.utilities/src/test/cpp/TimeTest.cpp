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


#include "gtest/gtest.h"

#include "time/Duration.h"

#include <chrono>

using namespace blocs;

class DurationTestFixture : public ::testing::Test {
public:
	virtual void SetUp() {}
	virtual void TearDown() {}
};

TEST_F(DurationTestFixture, testAsSeconds) {
	blocs::Duration dur0 = blocs::Duration(5.0);
	EXPECT_TRUE(dur0.asSeconds() == 5.0);
}

TEST_F(DurationTestFixture, testAsMicroseconds) {
	blocs::Duration dur0 = blocs::Duration(5.0);
	EXPECT_TRUE(dur0.asMicroseconds() == 5000000);
}

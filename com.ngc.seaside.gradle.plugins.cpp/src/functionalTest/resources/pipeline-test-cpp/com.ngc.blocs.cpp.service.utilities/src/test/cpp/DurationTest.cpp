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

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
#include "gmock/gmock.h"

#include "SyncEventService.h"
#include "event/DefaultEvent.h"
#include "event/DefaultEventTopic.h"
//#include "LogService.h"
//#include "logger/OstreamOutputStrategy.h"

using namespace blocs;
using namespace blocs::synceventservice;

using ::testing::Return;


class SyncEventServiceTestFixture : public ::testing::Test {
protected:
	std::shared_ptr<SyncEventService> eventService;
	//std::shared_ptr<LogService> logService;
	//std::string loggerId;
	//std::ostringstream logStream;


	virtual void SetUp() {
		/*logStream.str("");
		loggerId = "myLogger";

		logService = std::shared_ptr<LogService>(new LogService());
		logService->activate();
		logService->createLogger(loggerId);
		logService->addLogOutputStrategy(loggerId,new OstreamOutputStrategy(logStream)); // logService now has ownership
		*/

		eventService = std::shared_ptr<SyncEventService>(new SyncEventService());
		eventService->activate();
	}

	virtual void TearDown() {
		//logService->removeLogger(loggerId);
	}

};

class MockListener : public IEventListener {
public:
	MOCK_METHOD1(eventReceived, void(const IEvent &));
};

class MockEventTopic : public IEventTopic {
public:
	MOCK_CONST_METHOD0(getName, std::string());
};

class MockEvent : public IEvent {
public:
	MOCK_CONST_METHOD0(getTopic, std::shared_ptr<const IEventTopic>());
};

TEST_F(SyncEventServiceTestFixture, testSingleListener) {

	std::shared_ptr<const MockEventTopic> topic(new MockEventTopic());
	std::shared_ptr<MockListener> listener(new MockListener());
	std::shared_ptr<MockEvent> event(new MockEvent());

	ON_CALL(*std::static_pointer_cast<MockEvent>(event), getTopic())
		.WillByDefault(Return(topic));

	ON_CALL(*std::static_pointer_cast<const MockEventTopic>(topic), getName())
		.WillByDefault(Return(std::string("MockTopic")));

	EXPECT_CALL(*std::static_pointer_cast<MockEvent>(event), getTopic())
			.Times(2);

	EXPECT_CALL(*std::static_pointer_cast<const MockEventTopic>(topic), getName())
			.Times(::testing::AtLeast(2));

	EXPECT_CALL(*std::static_pointer_cast<MockListener>(listener), eventReceived(::testing::A<const IEvent &>()))
			.Times(1);

	eventService->addListener(topic,listener);

	// publish an event
	eventService->publish(event);


	// remove the listener
	eventService->removeListener(topic,listener);

	// publish an event
	eventService->publish(event);


}


TEST_F(SyncEventServiceTestFixture, testMultiListener) {

	std::shared_ptr<const MockEventTopic> topic(new MockEventTopic());
	std::shared_ptr<MockListener> listener1(new MockListener());
	std::shared_ptr<MockListener> listener2(new MockListener());
	std::shared_ptr<MockEvent> event(new MockEvent());

	ON_CALL(*std::static_pointer_cast<MockEvent>(event), getTopic())
			.WillByDefault(Return(topic));

	ON_CALL(*std::static_pointer_cast<const MockEventTopic>(topic), getName())
			.WillByDefault(Return(std::string("MockTopic")));

	EXPECT_CALL(*std::static_pointer_cast<MockEvent>(event), getTopic())
			.Times(2);

	EXPECT_CALL(*std::static_pointer_cast<const MockEventTopic>(topic), getName())
			.Times(::testing::AtLeast(4));

	EXPECT_CALL(*std::static_pointer_cast<MockListener>(listener1), eventReceived(::testing::A<const IEvent &>()))
			.Times(1);

	EXPECT_CALL(*std::static_pointer_cast<MockListener>(listener2), eventReceived(::testing::A<const IEvent &>()))
			.Times(2);

	eventService->addListener(topic,listener1);
	eventService->addListener(topic,listener2);

	// publish an event
	eventService->publish(event);


	// remove the listener
	eventService->removeListener(topic,listener1);

	// publish an event (only listener2 should receive)
	eventService->publish(event);

}

class EventData {
public:
	int i;
	float f;
	std::string s;

	EventData() : i(0), f(0.0), s() {}

	EventData *setI(int ii) { i = ii; return this; }
	EventData *setF(float ff) { f = ff; return this; }
	EventData *setS(const std::string& ss) { s = ss; return this; }

};


class TestEventTopic : public DefaultEventTopic<EventData> {
public:
	TestEventTopic() {
		setName("TestEventTopic");
	}

};

class TestEvent : public DefaultEvent<EventData> {
public:
	TestEvent() {

	}

	TestEvent(const TestEvent& te) {
		const EventData& d =  te.getSource();

		std::shared_ptr<EventData> data(new EventData());
		data->setI(d.i)->setF(d.f)->setS(d.s);
		setSource(data);

		setTopic(te.getTypedTopic());
	}

	TestEvent(int ii, float ff, const std::string& ss) {
		std::shared_ptr<EventData> data(new EventData());
		data->setI(ii)->setF(ff)->setS(ss);

		setSource(data);

		std::shared_ptr<const TestEventTopic> topic(new TestEventTopic());
		setTopic(std::dynamic_pointer_cast<const IEventTopicT<EventData>>(topic));
	}

	TestEvent& operator=(const TestEvent& te) {
		const EventData& d =  te.getSource();

		std::shared_ptr<EventData> data(new EventData());
		data->setI(d.i)->setF(d.f)->setS(d.s);
		setSource(data);

		setTopic(te.getTypedTopic());

		return *this;
	}
};

struct CaptureEvent {
	void eventReceived(const IEvent & event) {
		TestEvent te = dynamic_cast<const TestEvent &>(event);
		data = te.getSource();

		topic = std::dynamic_pointer_cast<const TestEventTopic>(event.getTopic());
	}

	EventData data;
	std::shared_ptr<const TestEventTopic> topic;
};

TEST_F (SyncEventServiceTestFixture, testDefaultEvent) {


	std::shared_ptr<MockListener> listener1(new MockListener());

	CaptureEvent tempEvent;
	EXPECT_CALL(*std::static_pointer_cast<MockListener>(listener1), eventReceived(::testing::A<const IEvent &>()))
			.WillOnce(testing::Invoke(&tempEvent, &CaptureEvent::eventReceived));


	std::shared_ptr<const TestEventTopic> topic(new TestEventTopic());

	eventService->addListener(topic,listener1);

	// publish an event
	std::shared_ptr<TestEvent> event(new TestEvent(5, 10.5, "IFS"));
	eventService->publish(event);

	// test if tempEvent has the appropriate data
	ASSERT_EQ(tempEvent.data.i, 5);
	ASSERT_FLOAT_EQ(tempEvent.data.f, 10.5);
	ASSERT_EQ(tempEvent.data.s, std::string("IFS"));
	ASSERT_EQ(tempEvent.topic->getName(), "TestEventTopic");

	// remove the listener
	eventService->removeListener(topic,listener1);

	// publish an event
	eventService->publish(event);

}


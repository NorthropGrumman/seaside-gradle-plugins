//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//  Copyright 2017 Northrop Grumman Corporation.
//------------------------------------------------------------------------------
#include <stdexcept>

#include "TimeService.h"
#include "time/ApplicationClock.h"
#include "time/ClockAlarm.h"
#include "time/Time.h"


/**
 * UNCLASSIFIED
 *
 * Real-World Time Service Implementation
 */
namespace blocs { namespace realworldtimeservice {


	TimeService::TimeService() {
	}

	TimeService::~TimeService() {
	}

	void TimeService::activate() {
	}

	void TimeService::start() {
	   ApplicationClock::setEpoch(Time());
	}

	void TimeService::stop() {
	}

	void TimeService::deactivate() {
	}

   const Time TimeService::getRealWorldUniversalTime(void) const {
      return Time();
   }

   const long TimeService::setRealWorldClockAlarm(const ClockAlarm & clockAlarm) {
      return -1;
   }

   const void TimeService::cancelRealWorldClockAlarm(long alarmId) {
   }

   const Time TimeService::getApplicationClockTime(void) const {
      return ApplicationClock::advanceTimeTo(Time());
   }

   const Time TimeService::getApplicationClockEpoch(void) const {
      return ApplicationClock::getEpoch();
   }

   const Duration TimeService::getElapsedApplicationTimeSinceEpoch(void) const {
      ApplicationClock::advanceTimeTo(Time());
      return ApplicationClock::getElapsedTime();

   }

   const long TimeService::setApplicationClockAlarm(const ClockAlarm & clockAlarm) {
      return -1;
   }

   const void TimeService::cancelApplicationClockAlarm(long alarmId) {

   }

   void TimeService::resetApplicationClock() {

   }

   void TimeService::pauseApplicationClock() {

   }

   bool TimeService::isApplicationClockPaused() const {
      return false;

   }

   void TimeService::resumeApplicationClock() {

   }




}}

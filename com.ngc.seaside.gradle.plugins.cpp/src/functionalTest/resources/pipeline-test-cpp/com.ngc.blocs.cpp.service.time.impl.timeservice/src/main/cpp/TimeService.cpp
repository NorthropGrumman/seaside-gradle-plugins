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

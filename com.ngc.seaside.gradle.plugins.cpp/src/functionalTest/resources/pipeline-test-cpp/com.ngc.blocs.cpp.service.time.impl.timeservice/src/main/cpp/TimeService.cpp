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

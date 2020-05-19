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
//  Copyright 2017 Northrop Grumman Systems Corporation.
//------------------------------------------------------------------------------
#ifndef _BLOCS_ITimeService_H
#define _BLOCS_ITimeService_H

#include "time/ClockAlarm.h"
#include "time/Duration.h"
#include "time/Time.h"

namespace blocs {

   /*! \brief ITimeService is an interface for obtaining current time 
   and setting alarms, a type of event that is set to occur at a specific clock time

   ITimeService encapsulates two conceptual clocks that represent "real world time"
   and "application time".  Generally speaking, clients of the ITimeService should be 
   using "application time".  Application time's relationship to real-world time
   is defined in implementations of ITimeService.  Application time may be
   in lock-step with real-world time, offset from real-world time, scaled from
   real world time (faster or slower), or they may be completely unrelated.

   */
   class ITimeService {

      public :

         /// obtains the current "real world" time in UTC
         /// (Coordinated Universal Time)
         ///
         /// The simplest potential implementation
         /// is to return the operating system time 
         /// adjusted to UTC
         /// 
         /// @return current real world UTC time
         virtual const Time getRealWorldUniversalTime(void) const = 0;

         /// registers an alarm event that will be invoked at the
         /// at the "real world" time indicated in the clock alarm
         ///
         /// @param[in] clockAlarm to set
         /// @return the ID the service assigned to the alarm (may be needed to cancel it)
         virtual const long setRealWorldClockAlarm(const ClockAlarm & clockAlarm) = 0;

         /// cancels a previously scheduled "real world" alarm
         /// 
         /// @param[in] alarmId to cancel
         virtual const void cancelRealWorldClockAlarm(long alarmId) = 0;


         /// Obtains the current application time.
         /// This is the time that will generally be used 
         /// by clients accessing the time service
         ///
         /// @return current application time
         virtual const Time getApplicationClockTime(void) const = 0;

         /// Obtains the application epoch time
         /// This will generally the application time at 
         /// the time the ITimeService was started.
         ///
         /// @return application time epoch
         virtual const Time getApplicationClockEpoch(void) const = 0;

         /// Obtains the seconds (and subseconds) of application
         /// time that have elapsed between the current application time
         /// and the application epoch
         ///
         /// @return seconds since epoch
         virtual const Duration getElapsedApplicationTimeSinceEpoch(void) const = 0;

         /// registers an alarm event that will be invoked at the
         /// at the application time indicated in the clock alarm
         ///
         /// @param[in] clockAlarm to set
         /// @return the ID the service assigned to the alarm (may be needed to cancel it)
         virtual const long setApplicationClockAlarm(const ClockAlarm & clockAlarm) = 0;

         /// cancels a previously scheduled application alarm
         /// 
         /// @param[in] alarmId to cancel
         virtual const void cancelApplicationClockAlarm(long alarmId) = 0;

         /// Normalizes the application clock current time
         /// so that it is synchronized to the application epoch time.
         /// Elapsed time since epoch becomes 0.
         ///
         /// It is implementation dependent whether this may change
         /// the value of the epoch time.
         virtual void resetApplicationClock() = 0;

         /// Freezes the application clock at the current application time.
         ///
         virtual void pauseApplicationClock() = 0;

         /// Freezes the application clock at the current application time.
         ///
         /// @return bool indicating whether the application clock is currently suspended
         virtual bool isApplicationClockPaused() const = 0;

         /// Resumes the progression of application time if it is currently paused.
         /// If application time is not paused the method will have no effect.
         ///
          virtual void resumeApplicationClock() = 0;
      };

} //NAMESPACE

#endif

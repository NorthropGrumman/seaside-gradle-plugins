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
#include "time/ApplicationClock.h"

#ifdef _WIN32
#include "gem/utility/platformspecific/windows/math.h"
#endif

namespace blocs {

    void ApplicationClock::setEpoch(const Time & epoch) {
       epochTime = epoch.getTimePoint();
       d0 = epochTime.time_since_epoch();
    }

    Time ApplicationClock::getEpoch() {
       return Time(epochTime);

    }

    Time ApplicationClock::advanceTimeBy(const Duration & by) {
       dElapsed += std::chrono::microseconds(by.asMicroseconds());
       return getCurrentTime();
    }

    Time ApplicationClock::advanceTimeTo(const Time & newClockTime) {
       dElapsed = newClockTime.getTimePoint() - epochTime;
       return newClockTime;
    }

    Time ApplicationClock::getCurrentTime() {
       return Time(now());
    }

    Duration ApplicationClock::getElapsedTime() {
       return Duration(dElapsed);
    }


    //this is part of the c++ clock structure
    ApplicationClock::time_point ApplicationClock::now()
    {	// get current time
       time_point currentTime = time_point(d0 + dElapsed);
       return currentTime;
    }

    //this is part of the c++ clock structure
    std::time_t ApplicationClock::to_time_t(const time_point& _Time)
    {	// convert to __time64_t
       return ((std::time_t)(_Time.time_since_epoch().count()
          / duration::period::den));
    }

    //this is part of the c++ clock structure
   ApplicationClock::time_point ApplicationClock::from_time_t(std::time_t _Tm)
    {	// convert from __time64_t
       return (time_point(duration(_Tm * duration::period::den)));
    }


   ApplicationClock::time_point ApplicationClock::epochTime = std::chrono::system_clock::now();
   ApplicationClock::duration ApplicationClock::d0 = std::chrono::duration_cast<ApplicationClock::duration>(
         std::chrono::system_clock::now().time_since_epoch());
   ApplicationClock::duration ApplicationClock::dElapsed = duration::zero();

}

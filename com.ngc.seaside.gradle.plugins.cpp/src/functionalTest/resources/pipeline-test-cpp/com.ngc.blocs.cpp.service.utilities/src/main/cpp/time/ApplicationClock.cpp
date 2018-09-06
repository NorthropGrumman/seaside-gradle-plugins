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

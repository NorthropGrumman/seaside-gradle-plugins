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
//  Copyright 2012 Northrop Grumman Systems Corporation.
//
//  US GOVERNMENT UNLIMITED RIGHTS
//  DFARS Clause reference: 
//  252.227-7013 (a)(16) and 252.227-7014 (a)(16) Unlimited Rights.
//  The Government has the right to use, modify, reproduce, perform, display, 
//  release or disclose this (technical data or computer software) in whole or
//  in part, in any manner, and for any purpose whatsoever, and to have or
//  authorize others to do so.
//
//
//  This work, authored and owned by Northrop Grumman Systems Corporation, was 
//  was funded in whole or in part by:
//    1) Northrop Grumman Systems Corporation private investments (IRADs)
//    2) United States Army Contract: W31P4Q-08-C-0418
//    3) United States Missile Defense Agency (MDA) Contract: H95001-10-D-0001
//    4) United States Missile Defense Agency (MDA) Contract: HQ0147-09-C-0006
//------------------------------------------------------------------------------
#ifndef _BLOCS_Clock_h
#define _BLOCS_Clock_h

#include <chrono>
#include <ctime>
#include <iostream>

#include "time/Duration.h"
#include "time/Time.h"

namespace blocs {


   struct ApplicationClock
   {
      //these values are part of the c++ clock structure
      using duration = Duration::DurationType;
      using rep = duration::rep;
      using period = duration::period;
      using time_point = Time::TimePointType;
      static constexpr bool is_steady = std::chrono::system_clock::is_steady;

      //this values are custom for ApplicationClock;
      static Time::TimePointType epochTime;  //this is the application epoch time, not the epoch time in the underlying class
      static  Duration::DurationType d0;
      static  Duration::DurationType dElapsed;

      static void setEpoch(const Time & epoch);

      static Time getEpoch();

      static Time advanceTimeBy(const Duration & by);

      static Time advanceTimeTo(const Time & newClockTime);

      static Time getCurrentTime();

      static Duration getElapsedTime();


      //this is part of the c++ clock structure
      static time_point now();

      //this is part of the c++ clock structure
      static std::time_t to_time_t(const time_point& _Time);

      //this is part of the c++ clock structure
      static time_point from_time_t(std::time_t _Tm);
   };





   /*****************************************************************
   * ARCHIVE
   struct ApplicationClock
   {
   using duration = std::chrono::microseconds;
   using rep = duration::rep;
   using period = duration::period;
   using time_point = std::chrono::time_point<ApplicationClock>;
   static constexpr bool is_steady = true;

   static duration d0;
   static duration dElapsed;

   static time_point advance_time(const duration & by) _NOEXCEPT  {
   dElapsed += by;
   return now();
   }


   static time_point now() _NOEXCEPT
   {	// get current time

   time_point currentTime = time_point(d0 + dElapsed);

   return currentTime;
   }

   static __time64_t to_time_t(const time_point& _Time) _NOEXCEPT
   {	// convert to __time64_t
   return ((__time64_t)(_Time.time_since_epoch().count()
   / duration::period::den));
   }

   static time_point from_time_t(__time64_t _Tm) _NOEXCEPT
   {	// convert from __time64_t
   return (time_point(duration(_Tm * duration::period::den)));
   }
   };
   */
}

#endif

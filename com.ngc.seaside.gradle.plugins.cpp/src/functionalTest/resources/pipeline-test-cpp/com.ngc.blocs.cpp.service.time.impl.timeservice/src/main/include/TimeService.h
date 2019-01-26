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
//  Copyright 2017 Northrop Grumman Systems Corporation.
//------------------------------------------------------------------------------
#ifndef _BLOCS_realworldtimeservice_TimeService_H
#define _BLOCS_realworldtimeservice_TimeService_H

#include <string>
#include <cstdio>

#include "ITimeService.h"

namespace blocs { namespace realworldtimeservice {


   class TimeService : public ITimeService {
   public:
      TimeService();
      virtual ~TimeService();

      void activate();
      void start();
      void stop();
      void deactivate();

      const Time getRealWorldUniversalTime(void) const;
      const long setRealWorldClockAlarm(const ClockAlarm & clockAlarm);
      const void cancelRealWorldClockAlarm(long alarmId);

      const Time getApplicationClockTime(void) const;
      const Time getApplicationClockEpoch(void) const;
      const Duration getElapsedApplicationTimeSinceEpoch(void) const;
      const long setApplicationClockAlarm(const ClockAlarm & clockAlarm);
      const void cancelApplicationClockAlarm(long alarmId);
      void resetApplicationClock();
      void pauseApplicationClock();
      bool isApplicationClockPaused() const;
      void resumeApplicationClock();

   };

}}

#endif


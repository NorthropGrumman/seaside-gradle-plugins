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


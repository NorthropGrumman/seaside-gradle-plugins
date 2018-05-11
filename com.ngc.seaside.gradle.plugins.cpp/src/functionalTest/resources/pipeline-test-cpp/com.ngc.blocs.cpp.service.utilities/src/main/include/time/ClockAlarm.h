//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//  Copyright 2017 Northrop Grumman Corporation
//------------------------------------------------------------------------------

#ifndef _BLOCS_ClockAlarm_H
#define _BLOCS_ClockAlarm_H

#include <memory>

#include "Time.h"

namespace blocs {

   class ClockAlarmListener;

   /**
    ClockAlarm is a desired notification scheduled for a specific Time.
    */

   class ClockAlarm {

      public:

         virtual ~ClockAlarm();

         virtual long getAlarmId() const;

      protected:

         ClockAlarm(const Time& alarmTime,
               std::shared_ptr<ClockAlarmListener> alarmListener);

   };

} //NAMESPACE

#endif

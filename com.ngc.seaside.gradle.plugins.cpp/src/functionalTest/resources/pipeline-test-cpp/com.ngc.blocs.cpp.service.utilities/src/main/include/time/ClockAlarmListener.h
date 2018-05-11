//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//  Copyright 2017 Northrop Grumman Corporation
//------------------------------------------------------------------------------

#ifndef _BLOCS_ClockAlarmListener_H
#define _BLOCS_ClockAlarmListener_H

#include <memory>
#include "Time.h"

namespace blocs {

   class ClockAlarm;

   /**
    ClockAlarmListener encapsulates the callback method to be invoked when a ClockAlarm "goes off"
    */

   class ClockAlarmListener {

      public:

         virtual ~ClockAlarmListener();

         virtual bool processAlarm(std::shared_ptr<const ClockAlarm> clockAlarm) = 0;


   };

} //NAMESPACE

#endif

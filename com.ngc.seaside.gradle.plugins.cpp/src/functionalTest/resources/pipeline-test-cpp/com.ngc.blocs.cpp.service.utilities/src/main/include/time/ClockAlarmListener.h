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

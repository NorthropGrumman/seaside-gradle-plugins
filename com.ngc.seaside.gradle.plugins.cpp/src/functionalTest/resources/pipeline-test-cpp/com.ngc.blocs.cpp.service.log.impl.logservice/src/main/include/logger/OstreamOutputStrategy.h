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
//------------------------------------------------------------------------------
#ifndef _BLOCS_OstreamOutputStrategy_H
#define _BLOCS_OstreamOutputStrategy_H

#include <mutex>
#include <iostream>

#include "logger/LogOutputStrategy.h"


namespace blocs { namespace basiclogservice {

      /** \brief Type of LogOutputStrategy that outputs the log stream to an
      ostream object, which is typically defaulted to std::cout.
      */

      class OstreamOutputStrategy : public LogOutputStrategy {

         public :

            OstreamOutputStrategy (std::ostream& output = std::cout);

            void outputFormattedLogLine(const LogOutputData & logData);

            void outputRawLogLine(const std::string & logData);

         private :

            std::ostream& outputstream;

            /**
             * A mutex to protect against multiple simultaneous writes to
             * this strategy instance
             */
            mutable std::mutex writeMutex;

      };

}} //NAMESPACE

#endif


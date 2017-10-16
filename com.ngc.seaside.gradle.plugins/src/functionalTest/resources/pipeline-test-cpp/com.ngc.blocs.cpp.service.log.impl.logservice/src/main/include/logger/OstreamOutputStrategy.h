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


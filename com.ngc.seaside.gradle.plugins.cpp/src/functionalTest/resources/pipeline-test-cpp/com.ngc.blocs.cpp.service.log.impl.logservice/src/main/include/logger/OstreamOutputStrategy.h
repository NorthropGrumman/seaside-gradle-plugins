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


//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------
#include "logger/OstreamOutputStrategy.h"

#include <string>


namespace blocs { namespace basiclogservice {

      OstreamOutputStrategy::OstreamOutputStrategy (std::ostream& output) : outputstream (output) {}

      void OstreamOutputStrategy::outputFormattedLogLine (const LogOutputData & logData) {

         std::ostringstream formatBuffer;
         formatLogLine(logData, formatBuffer);

         // Lock the ioMutex
         std::lock_guard<std::mutex> lock(writeMutex);

         outputstream << formatBuffer.str();
         outputstream.flush();
      }

      void OstreamOutputStrategy::outputRawLogLine (const std::string & logData) {

         // Lock the ioMutex
         //ScopedLock lock(writeMutex);

         outputstream << logData;
         outputstream.flush();
      }

}} //NAMESPACE


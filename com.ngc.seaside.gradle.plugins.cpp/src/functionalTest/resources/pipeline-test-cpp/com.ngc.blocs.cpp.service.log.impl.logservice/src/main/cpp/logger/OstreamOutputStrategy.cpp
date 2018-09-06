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


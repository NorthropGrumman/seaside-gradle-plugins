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
//------------------------------------------------------------------------------
#ifndef _BLOCS_LogOutputStrategy_H
#define _BLOCS_LogOutputStrategy_H

#include "logger/LogLevel.h"
//#include "gem/utility/EOL.h"
#include <vector>
#include <sstream>
#include <string>

namespace blocs { namespace basiclogservice {

      /** \brief Data used to log the intended message in the proper format.
      */
/*
      struct LogSourceLocation {
      private:
    	  std::string sourceFileName;
    	  std::string sourceFunctionName;
    	  int sourceLine;
    	  bool isSet;

      public:
    	  LogSourceLocation() :
    		  sourceFileName(""),
			  sourceFunctionName(""),
			  sourceLine(-1),
			  isSet(false) {}

    	  LogSourceLocation(
    			  const std::string& sourceFile,
				  const std::string& sourceFunctionName,
				  int line) :
		    		  sourceFileName(sourceFile),
					  sourceFunctionName(sourceFunctionName),
					  sourceLine(line),
					  isSet(true) {}

    	  const std::string& getSourceFileName() { return sourceFileName; }
    	  const std::string& getSourceFunctionName() { return sourceFunctionName; }
    	  int getSourceLine() { return sourceLine; }
    	  bool isSet() { return isSet; }
      };
      */

      struct LogOutputData {
         const std::string * logTimeSystem;
         const std::string * logTimeRelative;
         LogLevel::Values logLevel;
         const std::string * logText;
         const std::string * sourceLocationData;

         // instead of sourceFile, sourceFunction, sourceLine, can we use:
         //LogSourceLocation logSourceLocation;

         LogOutputData() :
               logTimeSystem(NULL),
               logTimeRelative(NULL),
               logLevel(LogLevel::NONE),
               logText(NULL),
			   sourceLocationData(NULL) {}

      };

      /** \brief Abstract base class for defining various output strategies for logging.
      */

      class LogOutputStrategy {

         public :

            virtual ~LogOutputStrategy() {}

            virtual void initialize(const std::string & loggerName) {}

            virtual void outputFormattedLogLine(const LogOutputData & logData) = 0;

            virtual void outputRawLogLine(const std::string & logData) = 0;

            virtual void shutdown() {}

         protected:

            virtual void formatLogLine(
               const LogOutputData & logData,
               std::ostringstream & formatBuffer) {

               //Build the formatted log line
               formatBuffer << *logData.logTimeSystem;

               if (logData.logTimeRelative != NULL) {
                  formatBuffer << " ";
                  formatBuffer << *logData.logTimeRelative;
               }

               formatBuffer << " <";

               formatBuffer << LogLevel::convertToString(logData.logLevel);
               formatBuffer << "> ";
               formatBuffer << *logData.logText;

               if (logData.sourceLocationData != NULL) {
				   formatBuffer << " [";
				   formatBuffer << *logData.sourceLocationData;
				   formatBuffer << "]";
               }

               formatBuffer << std::endl;
            }

         private :
      };

      typedef std::vector<LogOutputStrategy *> LogOutputStrategyPtrVector;


}} //NAMESPACE

#endif


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


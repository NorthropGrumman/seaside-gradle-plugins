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
#include "logger/Logger.h"
#include "logger/OstreamOutputStrategy.h"
//#include "gem/utility/String.h"
#include <map>
#include <string>
#include <mutex>
#include <functional>
#include <chrono>
#include <ctime>
#include <iomanip>
//#include <boost/date_time/posix_time/posix_time.hpp>


namespace blocs { namespace basiclogservice {

      typedef std::map<std::size_t, Logger *> LoggerIdToPtrMap;
      static LoggerIdToPtrMap loggerMap;

      typedef std::map<std::size_t, LogLevel::Values> LoggerIdToLogLevelMap;
      static LoggerIdToLogLevelMap logLevelMap;

      static std::mutex loggerMapsGuard;

      static Logger & NULL_LOGGER = Logger::createLogger (NULL_LOGGER_NAME);


      Logger & Logger::createLogger (const std::string & loggerName) {
                                     /*,TimeSource * relativeTimeSource) {*/

         Logger * newLogger = new Logger(loggerName/*, relativeTimeSource*/);

         registerLogger (newLogger);

         return *newLogger;
      }


      Logger & Logger::createLogger (const std::string & loggerName,
                                     LogOutputStrategy * strategy) {
    	  /*, TimeSource * relativeTimeSource) {*/

         Logger * newLogger = new Logger(loggerName/*, relativeTimeSource*/);

         newLogger->addStrategy (strategy);

         registerLogger (newLogger);

         return *newLogger;
      }


      Logger & Logger::createLogger (const std::string & loggerName,
                                     const LogOutputStrategyPtrVector & strategies) {
    	  /*, TimeSource * relativeTimeSource) {*/

         Logger * newLogger = new Logger(loggerName/*, relativeTimeSource*/);

         newLogger->strategies = strategies;

         //since directly adding strategies (rather than calling addStrategy)
         //must invoke "initialize" on the strategy here

         for ( LogOutputStrategyPtrVector::iterator iter = newLogger->strategies.begin();
               iter != newLogger->strategies.end();
               iter++ ) {

            (*iter)->initialize (loggerName);
         }

         registerLogger (newLogger);

         return *newLogger;
      }


      Logger::Logger (const std::string& name/*,
                      TimeSource* relativeTimeSource*/) :
            loggerName (name),
            loggerLevel (LogLevel::NONE) {
    	  /*, TimeSource * relativeTimeSource) {*/

         loggerId = std::hash<std::string>()(loggerName);

         // THIS IS NEEDED SO THAT THE MAPS WILL BE CREATED...  MUST BE A COMPILER BUG!!!
         if (loggerMap.size() == 0) loggerMap.clear();
         if (logLevelMap.size() == 0) logLevelMap.clear();

         //If log level was pre-initialized, be sure to use that value
         LoggerIdToLogLevelMap::const_iterator posI;
         posI = logLevelMap.find (loggerId);

         if (posI != logLevelMap.end() ) {
            loggerLevel = posI->second;
         }
      }

      Logger::~Logger() {

         while (!strategies.empty()) {
            LogOutputStrategy* los = strategies.back();
            strategies.pop_back();
            los->shutdown();
            delete los;
         }

         loggerName = "";

         //relativeTimeSource = NULL;
      }

      void Logger::registerLogger (Logger * logger) {

         std::lock_guard<std::mutex> lock(loggerMapsGuard);

         //Add the logger to the Logger Map

         LoggerIdToPtrMap::iterator iter;

         //If there is an existing logger with this name, shutdown/destroy it
         iter = loggerMap.find (logger->loggerId);

         if (iter != loggerMap.end() ) {
            delete iter->second;
            loggerMap.erase(iter);
         }

         loggerMap.insert (std::make_pair (logger->loggerId, logger));

         // If the level for this logger was not statically pre-set to some value,
         // default the logging level to none.

         if (logLevelMap.count (logger->loggerId) == 0) {
            logLevelMap.insert (std::make_pair (logger->loggerId, LogLevel::NONE) );
         }
      }


      Logger& Logger::get(const std::string & loggerName) {

         std::size_t searchValue = std::hash<std::string>()(loggerName);

         LoggerIdToPtrMap::const_iterator posI;

         //Since shutdown and delete can structurally alter the map,
         //we should protect this
         std::lock_guard<std::mutex> lock(loggerMapsGuard);

         posI = loggerMap.find (searchValue);

         if (posI != loggerMap.end() ) {
            return *posI->second;
         }
         else {
            return NULL_LOGGER;
         }
      }


      void Logger::setLoggingLevel (const std::string & loggerName,
                                    LogLevel::Values loggingLevel) {

         if (loggerName == NULL_LOGGER_NAME)
            return ;

         std::size_t searchValue = std::hash<std::string>()(loggerName);

         //Set the value in the global map
         //
         //Since we don't ever delete from the global logger level,
         //we can get away without protecting it.
         logLevelMap[searchValue] = loggingLevel;

         //If this logger actually exists, we want to change it's log level as well
         //(we need to protect this, since additions/shutdowns can structurally alter the map)
         std::lock_guard<std::mutex> lock(loggerMapsGuard);

         LoggerIdToPtrMap::const_iterator posI;

         posI = loggerMap.find (searchValue);

         if (posI != loggerMap.end() ) {
            (*posI->second).loggerLevel = loggingLevel;
         }
      }

      void Logger::shutdownAndDeleteLogger(const std::string & loggerName) {

         std::size_t searchValue = std::hash<std::string>()(loggerName);

         //we need to protect this, it will change the order of the loggerMap
         std::lock_guard<std::mutex> lock(loggerMapsGuard);

         if (loggerName != NULL_LOGGER_NAME) {

            LoggerIdToPtrMap::iterator posI;

            posI = loggerMap.find (searchValue);

            if (posI != loggerMap.end() ) {
               delete posI->second;
               loggerMap.erase(posI);
            }

         }
      }

      bool Logger::willLogMessageAtLevel(LogLevel::Values minimumLevel) const {

         bool willLog = false;

         if (!strategies.empty() ) {
            if (minimumLevel >= loggerLevel ) {
               switch (minimumLevel) {

                  case LogLevel::ALL:
                     willLog = LOG_FINE_POSSIBLE;
                     break;

                  case LogLevel::DEBUG_FINE:
                     willLog = LOG_FINE_POSSIBLE;
                     break;

                  case LogLevel::DEBUG_REGULAR:
                     willLog = LOG_DEBUG_POSSIBLE;
                     break;

                  case LogLevel::INFO:
                     willLog = LOG_INFO_POSSIBLE;
                     break;

                  case LogLevel::WARN:
                     willLog = LOG_WARN_POSSIBLE;
                     break;

                  case LogLevel::USERGRAM:
                     willLog = LOG_USERGRAM_POSSIBLE;
                     break;

                  case LogLevel::LOG_ERROR:
                     willLog = LOG_ERROR_POSSIBLE;
                     break;

                  case LogLevel::FATAL:
                     willLog = LOG_FATAL_POSSIBLE;
                     break;

                  case LogLevel::NONE:
                     break;

               }
            }
         }

         return willLog;
      }


      void Logger::addStrategy (LogOutputStrategy * strategy) {

         //Don't allow adding strategies to the NULL_LOGGER

         if (loggerName == NULL_LOGGER_NAME)
            return ;


         strategy->initialize (loggerName);

         //Since strategies are generally only added during the process of
         //creating the logger, we can cheat and not protect strategies.
         strategies.push_back (strategy);
      }


      void Logger::setLoggingLevel (LogLevel::Values level) {

         //Don't allow level changes to the NULL_LOGGER
         if (loggerName == NULL_LOGGER_NAME)
            return ;

         //record in local object and global list
         logLevelMap[loggerId] = level;

         loggerLevel = level;
      }


      LogLevel::Values Logger::getLoggingLevel() const {
         return loggerLevel;
      }


      std::string & Logger::getName() {
         return loggerName;
      }


      //void Logger::setRelativeTimeSource (TimeSource * timeSource) {
      //   relativeTimeSource = timeSource;
      //}


      void Logger::logFormattedText (
         const LogLevel::Values & level,
         const std::string & text,
         const std::string & sourceLocation) {

         if (willLogMessageAtLevel(level)) {

            LogOutputData logData;

            //SYSTEM TIME  TODO:  Calculate milliseconds
            auto currentTime =  std::chrono::system_clock::now();
            auto currentTime_t = std::chrono::system_clock::to_time_t(currentTime);
            std::ostringstream ossTime;
            // NOT IN GCC  4.8.5 20150623  In GCC 5
            //ossTime << std::put_time(std::gmtime(&in_time_t), "%y%m%d-%T");
            char buf[40] = {0};

            //format: YYMMDD-HH:MM:SS.sss
            std::strftime(buf, sizeof(buf), "%y%m%d-%T", std::gmtime(&currentTime_t));

            //std::string systemTime = systemTimeSource.getCurrentTime().toString();
            std::string systemTime = buf;//ossTime.str();
            logData.logTimeSystem = &systemTime;

            //RELATIVE TIME
            std::string relativeTime;

            //if (relativeTimeSource == NULL) {
               logData.logTimeRelative = NULL;
            //}
            //else {
            //   relativeTime = relativeTimeSource->getCurrentTime().toString();
            //   logData.logTimeRelative = &relativeTime;
            //}

            //LEVEL
            logData.logLevel = level;

            //LOG TEXT
            logData.logText = &text;

            // Set the source information
            logData.sourceLocationData = &sourceLocation;

            //FILE NAME
            //std::string fileName = getFileFromCanonicalName (file);

            //logData.sourceFile = &fileName;

            //FUNCTION
            //logData.sourceFunction = &function;

            //LINE
            //logData.sourceLine = line;

            for ( LogOutputStrategyPtrVector::const_iterator iter = strategies.begin();
                  iter != strategies.end();
                  iter++ ) {

               (*iter)->outputFormattedLogLine (logData);
            }
         }
      }


      void Logger::logRawText (const std::string & text) {

         if (!strategies.empty() ) {

            for ( LogOutputStrategyPtrVector::const_iterator iter = strategies.begin();
                  iter != strategies.end();
                  iter++ ) {

               (*iter)->outputRawLogLine (text);
            }
         }
      }

      void Logger::logLogOutputData(const LogOutputData & logOutputData) {
         if (willLogMessageAtLevel(logOutputData.logLevel)) {

            for ( LogOutputStrategyPtrVector::const_iterator iter = strategies.begin();
                  iter != strategies.end();
                  iter++ ) {

               (*iter)->outputFormattedLogLine (logOutputData);
            }
         }
      }


      std::string Logger::getFileFromCanonicalName (const std::string & canonicalFile) {

         std::string::size_type position = std::string::npos;
         position = canonicalFile.find_last_of ("\\");

         if (position == std::string::npos) {
            position = canonicalFile.find_last_of ("/");
         }

         if ( (position == std::string::npos) || (position == canonicalFile.size() ) ) {
            return canonicalFile;
         }
         else {
            return canonicalFile.substr ( (position + 1), (canonicalFile.size() - position) );
         }
      }

}} //NAMESPACE


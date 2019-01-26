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
/**
 * UNCLASSIFIED
 *
 * LogService implements the ILogService interface
 */
#ifndef _BLOCS_CORE_CPP_LOGSERVICE_H
#define _BLOCS_CORE_CPP_LOGSERVICE_H

#include <string>
#include <cstdio>

#include "logservice/ILogService.h"
#include "logger/Logger.h"
#include "logger/LogLevel.h"

namespace blocs { namespace basiclogservice {

	// Forward Declarations
	class LogOutputStrategy;

	class LogService : public ILogService {
	public:
		LogService();
		virtual ~LogService();

		void activate();
		void start();
		void stop();
		void deactivate();

        void createLogger(const std::string & loggerId) {
        	Logger::createLogger(loggerId);
        }

        void removeLogger(const std::string & loggerId) {
        	Logger::shutdownAndDeleteLogger(loggerId);
        }

        void logTrace(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to FINE or higher. */
        bool isTraceEnabled(const std::string & loggerId) const{
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::DEBUG_FINE);
        }

        void logDebug(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to DEBUG or higher. */
        bool isDebugEnabled(const std::string & loggerId) const{
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::DEBUG_REGULAR);
        }

        void logInfo(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to INFO or higher. */
        bool isInfoEnabled(const std::string & loggerId) const{
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::INFO);
        }

        void logWarn(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to WARN or higher. */
        bool isWarnEnabled(const std::string & loggerId) const{
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::WARN);
        }

        void logUsergram(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to USERGRAM or higher. */
        bool isUsergramEnabled(const std::string & loggerId) const {
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::USERGRAM);
        }

        void logError(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to LOG_ERROR or higher. */
        bool isErrorEnabled(const std::string & loggerId) const{
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::LOG_ERROR);
        }

        void logFatal(const std::string & location, const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if logging level is set to FATAL. */
        bool isFatalEnabled(const std::string & loggerId) const {
        	return Logger::get(loggerId).willLogMessageAtLevel(LogLevel::FATAL);
        }

        void logRaw(const std::string & loggerId, const char * messageFormat, ...);

        /** \brief Returns true if any logging level other than NONE is set. */
        bool isRawEnabled(const std::string & loggerId) const {
        	return (Logger::get(loggerId).getLoggingLevel() != LogLevel::NONE);
        }

        /**
         * # LogService Configuration Methods
         *
         * The following methods are required to setup this specific implementation of ILogService.
         */

        /** \brief Add LogOutputStrategy.  Note the Logger takes ownership of the object */
        void addLogOutputStrategy(const std::string& loggerId, LogOutputStrategy *outputStrategy) {
        	Logger::get(loggerId).addStrategy(outputStrategy);
        }

        /** \brief Set the logging level */
        void setLoggingLevel(const std::string& loggerId, LogLevel::Values logLevel) {
        	Logger::setLoggingLevel(loggerId, logLevel);
        }


	};

}}

#endif

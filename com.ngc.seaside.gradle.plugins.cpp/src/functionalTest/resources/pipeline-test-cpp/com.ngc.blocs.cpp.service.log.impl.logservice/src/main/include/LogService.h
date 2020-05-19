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

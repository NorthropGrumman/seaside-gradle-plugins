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
 * LogService Implementation
 */

#include <stdexcept>

#include "LogService.h"

namespace blocs { namespace basiclogservice {


	LogService::LogService() {

	}

	LogService::~LogService() {

	}

	void LogService::activate() {
		// Do Configuration here
	}

	void LogService::start() {

	}

	void LogService::stop() {

	}

	void LogService::deactivate() {

	}

    void LogService::logTrace(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::DEBUG_FINE, message, location);

    }

    void LogService::logDebug(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::DEBUG_REGULAR, message, location);

    }

    void LogService::logInfo(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::INFO, message, location);

    }

    void LogService::logWarn(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::WARN, message, location);

    }

    void LogService::logError(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::LOG_ERROR, message, location);

    }

    void LogService::logUsergram(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::USERGRAM, message, location);

    }

    void LogService::logFatal(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logFormattedText (LogLevel::FATAL, message, location);

    }

    void LogService::logRaw(const std::string & loggerId, const char * messageFormat, ...) {

        va_list args;
        va_start(args, messageFormat);
        auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
        va_end(args);

        va_start(args, messageFormat);

        std::string message;
        message.resize(size, '\0');

        vsprintf(&message[0], messageFormat, args);
        va_end(args);

        //final message construction and output code goes here
        //std::cout << loggerId << "  " << message << "  " << location << std::endl;
        Logger::get(loggerId).logRawText(message);

    }

}}

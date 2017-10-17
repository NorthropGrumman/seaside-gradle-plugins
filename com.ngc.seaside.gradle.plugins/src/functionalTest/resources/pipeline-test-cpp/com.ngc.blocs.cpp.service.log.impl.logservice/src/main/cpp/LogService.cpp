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

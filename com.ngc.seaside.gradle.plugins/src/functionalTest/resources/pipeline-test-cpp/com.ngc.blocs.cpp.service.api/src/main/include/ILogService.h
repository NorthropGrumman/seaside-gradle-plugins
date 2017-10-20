//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//  Copyright 2017 Northrop Grumman Systems Corporation.
//------------------------------------------------------------------------------
#ifndef _BLOCS_ILogService_H
#define _BLOCS_ILogService_H

#include <iostream>
#include <cstdarg>


/*
The S1 S2 macro for capturing the line number in a concatenatable form is from an stackoverflow post
by "deepmax" that referred to this as the "double-macro-stringy" technique to expand the
__LINE__ macro in 2 levels.  "deepmax" offered the offered the following explanation:

First of all, using operator # in a function - like macro, it must followed by a macro parameter
and __LINE__ is not a parameter, otherwise compiler complains it's a stray operator.

Second, __LINE__ itself is a macro and contains current line number, it should be expanded to the number
before using it with #, otherwise, you will get string "__LINE__" instead of a number.

Macro S2(__LINE__) expands __LINE__ to a line number, then we pass the line number to #x.
*/

#define S1(x) #x
#define S2(x) S1(x)
//#define LOGFROM __FILE__ ", " __FUNCTION__ ", " S2(__LINE__)
//the line below also works but it still requires S2 (and it may or may not be more costly)
#define LOGFROM std::string(__FILE__) + ", " + std::string(__FUNCTION__) + ", " + std::string(S2(__LINE__))


/**  LOGGING MACROS.

* NOTE: LOGBLOCS_XXX pseudo-methods are macros so that the built-in __FILE__, __FUNCTION__, and __LINE__
* macros will be utilized to capture the specific location in the code where any given log message originates.
* If this were actual class methods then the user would have to provide these values for every log call.  Otherwise,
* if invoked from the service,  __FILE__, __FUNCTION__, and __LINE__ would just give us locations in the service, not
* the origination point of the log message.
*
*
      <TABLE>
      <TR>
      <TD>LOGBLOCS_TRACE(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::DEBUG_FINE level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_DEBUG(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::DEBUG_REGULAR level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_INFO(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::INFO level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_WARN(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::WARN level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_USERGRAM(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::USERGRAM level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_ERROR(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::LOG_ERROR level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_FATAL(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs a formatted, LogLevel::FATAL level text message to all registered LogOutputStrategy instances of the Logger identified by loggername if within the level constraint of that Logger.
      </TR>
      <TR>
      <TD>LOGBLOCS_RAW(ILogService logService, std::string loggerName, const str* format, ...)</TD>
      <TD>logs unformatted text to all registered LogOutputStrategy instances of the Logger identified by loggername.  Logger level does not constrain output of raw text logging.
      </TR>
      </TABLE>
      <H1>A Logger is utilized in the following manner</H1>
      <H2>Initialization and Setting Level</H2>
      <PRE>
      Logger::setLoggingLevel("mylogger", LogLevel::DEBUG_REGULAR); //may be set at any time
      Logger &logger = Logger::createLogger("mylogger", (new OstreamOutputStrategy()));
      </PRE>
      <H2>Logging Messages</H2>
      <PRE>
      LOGBLOCS_TRACE(myLogService, "mylogger", "a fine log message from %s", "mylogger1");
      LOGBLOCS_DEBUG(myLogService, "mylogger", "a debug log message from %s", "mylogger1");
      LOGBLOCS_INFO(myLogService, "mylogger", "a info log message from %s", "mylogger1");
      LOGBLOCS_WARN(myLogService, "mylogger", "a warning log message from %s", "mylogger1");
      LOGBLOCS_USERGRAM(myLogService, "mylogger", "a usergram log message from %s", "mylogger1");
      LOGBLOCS_ERROR(myLogService, "mylogger", "a error log message from %s", "mylogger1");
      LOGBLOCS_FATAL(myLogService, "mylogger", "a fatal log message from %s", "mylogger1");
      LOGBLOCS_RAW(myLogService, "mylogger", "a raw log message from %s", "mylogger1");
      </PRE>
*
*/

#define LOGBLOCS_TRACE(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logTrace(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_TRACE_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isTraceEnabled(loggerId) : false)

#define LOGBLOCS_DEBUG(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logDebug(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_DEBUG_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isDebugEnabled(loggerId) : false)

#define LOGBLOCS_INFO(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logInfo(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_INFO_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isInfoEnabled(loggerId) : false)

#define LOGBLOCS_WARN(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logWarn(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_WARN_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isWarnEnabled(loggerId) : false)

#define LOGBLOCS_ERROR(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logError(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_ERROR_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isErrorEnabled(loggerId) : false)

#define LOGBLOCS_USERGRAM(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logUsergram(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_USERGRAM_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isUsergramEnabled(loggerId) : false)

#define LOGBLOCS_FATAL(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logFatal(LOGFROM, loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_FATAL_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isFatalEnabled(loggerId) : false)

#define LOGBLOCS_RAW(iLogServicePointer, loggerId, fmt, ...) if (iLogServicePointer) iLogServicePointer->logRaw(loggerId, fmt, ##__VA_ARGS__)
#define LOGBLOCS_RAW_ENABLED(iLogServicePointer, loggerId) (iLogServicePointer ? iLogServicePointer->isRawEnabled(loggerId) : false)


namespace blocs {

   /*! \brief ILogService is an interface for writing text based log messages to a logging
   destination referred to by a loggerId handle

   Users of ILogService can use the macros in ILogService or the class methods

   <H2>Sample logging sequence</H2>
   <PRE>

   //setup
   ILogService * logService;
   std::string myLoggerId = "myLogger";
   logService->createLogger(myLoggerId);

   //example logging using the class interface methods

   if (logService->isXXXEnabled(LOGID)) {
      logService->logInfo(LOGFROM, LOGID, "helloWithNoArguments");
      logService->logInfo(LOGFROM, LOGID, "%s %f hello with arguments", "start", 57.0);
   }

   //example logging using the macros

   if (LOGBLOCS_INFO_ENABLED(logService, LOGID)) {
      LOGBLOCS_INFO(logService, LOGID, "helloWithNoArguments");
      LOGBLOCS_INFO(logService, LOGID, "%s %f hello with arguments", "start", 57.0);
   }


   //more user code...

   //when we're not going to use this logger anymore
   logService->removeLogger(myLoggerId);

   </PRE>
   */
   class ILogService {

      public :

         /*!
         Creates a Logger in the service to be identified by the input loggerId.  loggerIds should
         be unique within the context of the service instance.
         */
         virtual void createLogger(const std::string & loggerId) = 0;

         /*!
         Removes a Logger from the service identified by the input loggerId.
         */
         virtual void removeLogger(const std::string & loggerId) = 0;

         /** \brief Log messages at any log level setting other than NONE.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logTrace(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Trace logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that trace logging is enabled (DEBUG_FINE)
          */
         virtual bool isTraceEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the DEBUG_REGULAR, INFO, WARN, USERGRAM, LOG_ERROR, and FATAL log levels.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logDebug(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Debug logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that debug logging is enabled (DEBUG_REGULAR)
          */
         virtual bool isDebugEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the INFO, WARN, USERGRAM, LOG_ERROR, and FATAL log levels.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logInfo(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Info logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that info logging is enabled (INFO)
          */
         virtual bool isInfoEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the WARN, USERGRAM, LOG_ERROR, and FATAL log levels.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logWarn(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if warn logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that warn logging is enabled (WARN)
          */
         virtual bool isWarnEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the LOG_ERROR and FATAL log levels.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logError(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Error logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that error logging is enabled (LOG_ERROR)
          */
         virtual bool isErrorEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the USERGRAM, LOG_ERROR, and FATAL log levels.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logUsergram(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Usergram logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that usergram logging is enabled (USERGRAM)
          */
         virtual bool isUsergramEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at the FATAL log level only.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logFatal(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Fatal logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that fatal logging is enabled (FATAL)
          */
         virtual bool isFatalEnabled(const std::string & loggerId) const = 0;

         /** \brief Log messages at any log level other than NONE.
          *
          * \param location can be any additional information logged at the end of the log message;  Typically
          *        it is the __FILE__, __FUNCTION__, __LINE combination defined by the macro LOGFROM.
          * \param loggerId is the logger name.
          * \param messageFormat can be the message string or a c++-style format string containing format specifiers.
          * \param ... is the variable length argument list that are inserted in the format string.
          */
         virtual void logRaw(const std::string & loggerId, const char * messageFormat, ...) = 0;

         /** \brief Returns boolean that indicates if Raw logging is enabled or disabled.
          *
          * \param loggerId is the logger name.
          * \return boolean where true indicates that raw logging is enabled (any loglevel other than NONE)
          */
         virtual bool isRawEnabled(const std::string & loggerId) const = 0;


         //TEMPORARY
         //This code excerpt that follows illustrates how an implementation might apply the
         //variable format parameters to the messageFormat and capture the results in a message string
         //
         /*
         void logXXX(const std::string & location, const std::string & loggerId, const char * messageFormat, ...) {
            va_list args;
            va_start(args, messageFormat);
            auto size = std::vsnprintf(nullptr, 0, messageFormat, args);
            va_end(args);
            va_start(args, messageFormat);
            std::string message(size, '\0');
            std::vsprintf(&message[0], messageFormat, args);
            va_end(args);

            //final message construction and output code goes here
            std::cout << loggerId << "  " << message << "  " << location << std::endl;
          }
          */

      };

} //NAMESPACE

#endif

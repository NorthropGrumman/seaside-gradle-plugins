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
#ifndef _GEM_LogLevel_H
#define _GEM_LogLevel_H

#include <string>
#include <stdexcept>
#include <map>
#include <mutex>


namespace blocs { namespace basiclogservice {

      /**
       * Enumerates the logging levels allowed for logging data.
       */

      class LogLevel {

         public :

            /**
             * This is the enumeration that will be used to specify the log level
             * values in the simulation.
             */
            enum Values {
               ALL,
               //DEVELOPER LEVELS
               DEBUG_FINE,
               DEBUG_REGULAR,
               //POST RUN ANALYSIS LEVELS
               INFO,
               WARN,
               //REAL-TIME MONITORING LEVELS
               USERGRAM,
               LOG_ERROR,
               FATAL,
               NONE
            };

            /**
             * Typedef that will define the map that contains the translation
             * information between trings and enumerations
             */
            typedef std::map<Values, std::string> EnumerationMap;

            /**
             * This typedef will make the ObjectTypeEnum enumeration compatible with
             * the EnumerationProperty
             */
            typedef Values Enum;

            /**
             * Converts a string value to an enumeration.
             * @param value the string to convert to the enumeration
             * @throw std::range_error when the string value is not able to be
             * converted to an enumeration
             */
            static Values convertToEnum(const std::string& value)
            throw (std::range_error);

            /**
             * Converts an enumeration into a string value
             * @param value the value to convert into a string
             * @throw std::range_error when the enumeration value is not able to be
             * converted to a string
             */
            static const std::string& convertToString(Values value)
            throw (std::range_error);

            /**
             * Returns the Enumeration Map that contains all of the enumerations
             * and the human readable strings
             * @return EnumerationMap containing all enums and strings
             */
            static const EnumerationMap& getEnumerationMap(void);

            /**
             * Returns the name of this enumeration
             * @return name of this enumeration
             */
            inline static const std::string getEnumerationName(void);

         private :

            /**
             * The map that will be used to translate between enumerations and thier
             * string values.
             */
            static EnumerationMap stringValues;
            static std::mutex mapInitializationGuard;
            static bool mapInitialized;

            /**
             * Called the first time that the conversion functions are used so that
             * the values can be translated properly.  This will populate the
             * translation map.
             */
            static void populateMap(void);


            /**
             * Hidden constructor so that this class cannot be instantiated.
             */
            LogLevel(void);

      };

      const std::string LogLevel::getEnumerationName(void) {
         return "LogLevel";
      }

}} //NAMESPACE

#endif


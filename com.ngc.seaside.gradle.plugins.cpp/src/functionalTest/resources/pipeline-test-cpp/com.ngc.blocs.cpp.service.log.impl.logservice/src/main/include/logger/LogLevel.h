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


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
//
//  Copyright 2017 Northrop Grumman Systems Corporation.
//------------------------------------------------------------------------------
#ifndef _BLOCS_Time_h
#define _BLOCS_Time_h

#include "time/Duration.h"
#include <chrono>
#include <ctime>
#include <iostream>
#include <map>

namespace blocs {

   /**
    * Wraps chrono time_point to add support for formatting and ease of use.
    */
   class Time {
   public:
      enum Timescale {
         UTC,
         GPS,
         TAI,
         TT
      };

      typedef  std::chrono::system_clock::time_point TimePointType;
      typedef std::map<Time, Duration> LeapSecondsToDateMap;

      /**
       * Default constructor
       */
      Time();


      /**
       * Constructs a Time object using data for a std::tm_t data type.
       * @param year is a 4 digit year (1978)
       * @param mon is the month of the year (1=Jan)
       * @param day is the day of the month
       * @param hour is the hour of day (0 to 23)
       * @param min is the minute of the hour (0 to 59)
       * @param sec is the second of the minute (0 to 59)
       * @param fractionalSeconds are the fractional seconds part of the seconds (between 0 and 1:  0.25)
      *  @param timescale
       */
      Time(int year, int mon, int day, int hour, int min, int sec, const Duration& fractionalSeconds, Timescale timescale = UTC);


      /**
       * Constructs a Time object using a chrono time point type
       * @param chronoTimePoint the time point
      *  @param timescale
       */
      Time(const TimePointType & chronoTimePoint, Timescale timescale = UTC);

      /**
       * Gets the time point
       */
      const TimePointType & getTimePoint() const;

      Timescale getTimeScale() const;

      /**
      * Returns the leap seconds based on the Time argument
       */
      static Duration getLeapSeconds(const Time& t);

      /**
       * Returns a formatted time string
       */
      const std::string getFormattedTimeString();


      static Duration getUTC2TAIOffset(const Time& t);
      static Duration getUTC2TTOffset(const Time& t);
      static Duration getUTC2GPSOffset(const Time& t);

      Time asUTC();
      Time asGPS();
      Time asTAI();
      Time asTT();

      double asJulianDay() const;
      double asGreenwichMeanSiderealTime() const;
      double asSecondsSinceGMSTZero() const;
      double asSecondsSinceJ2000Epoch() const;
      double asSecondsSinceUTCEpoch() const;
      double asSecondsSinceGPSEpoch() const;


      /**
       * Assignment Operator
       * @param rhs the right hand side of the operation
       * @return this object
       */
      inline Time& operator= (const Time &rhs);

      /**
       * Implements the += operator when adding a Duration to this time value,
       * returning the result
       * @param rhs the duration to add to this time
       * @return this time object after adding the duration value to it
       */
      //Time& operator+= (const Duration& rhs);

      /**
       * Implements the -= operator when subtracting a Duration from this time
       * value.
       * @param rhs the duration to subtract from this time
       * @return this time object after subtracting the duration value from it
       */
      //Time& operator-= (const Duration& rhs);

      /**
       * Implements the - operator when subtracting one time from another
       * @param rhs the time to subtract from this value
       * @return the duration between the two times
       */
      Duration operator- (const Time& rhs) const;

      /**
       * Implements the + operator when adding a duration to a time value
       * @param rhs the duration to add to this time value
       * @return a new time instance with the duration added in
       */
      Time operator+ (const Duration& rhs) const;

      /**
       * Implements the - operator when subtracting a duration from a time
       * value
       * @param rhs the duration to subtract from this time
       * @return a new time value with the duration subtracted
       */
      Time operator- (const Duration& rhs) const;

      /**
       * Equals Comparison operator for comparing two times together.  This
       * function takes into consideration a comparison precision that was
       * defined in the Duration object.
       * @see Duration::TIME_COMPARISON_PRECISION
       * @param rhs the Time object to compare this object to
       * @return *this == rhs
       */
      bool operator== (const Time& rhs) const;

      /**
       * Less than operator for comparing two times together.  This function
       * takes into consideration a comparison precision that was defined in
       * Duration object
       * @see Duration::TIME_COMPARISON_PRECISION
       * @param rhs the Time object to compare to
       * @return *this &lt; rhs
       */
      bool operator< (const Time& rhs) const;

      /**
       * Implements the != operator based on the == operator
       * @see Time::operator==
       * @param rhs the Time to compare
       * @return *this != rhs
       */
      inline bool operator!= (const Time& rhs) const;

      /**
       * Implements the &gt; operator based on the &lt; operator provided
       * @see Time::operator<
       * @param rhs the Time to compare to
       * @return *this &gt; rhs
       */
      inline bool operator> (const Time& rhs) const;

      /**
       * Implements the &lt;= operator based on the &lt; operator provided
       * @see Time::operator<
       * @param rhs the Time to compare to
       * @return *this &lt;= rhs
       */
      inline bool operator<= (const Time& rhs) const;

      /**
       * Implements the &gt;= operator based on the &lt; operator provided
       * @see Time::operator<
       * @param rhs the Time to compare to
       * @return *this &gt;= rhs
       */
      inline bool operator>= (const Time& rhs) const;

   private:
      TimePointType time;
      Timescale timescale;

      static LeapSecondsToDateMap leapSecondsToDateTable;
   };
}

#endif

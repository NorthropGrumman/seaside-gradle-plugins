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
#include <sstream>
#include "time/Time.h"


namespace blocs {

  // initialize the leapSecondsToDateTable
  Time::LeapSecondsToDateMap Time::leapSecondsToDateTable = {
		  { Time(1972, 6, 30, 23, 59, 59, Duration(0.999999)), Duration(0.0) },
		  { Time(1972, 7, 1, 0, 0, 0, Duration(0.0)), Duration(1.0) },
		  { Time(1973, 1, 1, 0, 0, 0, Duration(0.0)), Duration(2.0) },
		  { Time(1974, 1, 1, 0, 0, 0, Duration(0.0)), Duration(3.0) },
		  { Time(1975, 1, 1, 0, 0, 0, Duration(0.0)), Duration(4.0) },
		  { Time(1976, 1, 1, 0, 0, 0, Duration(0.0)), Duration(5.0) },
		  { Time(1977, 1, 1, 0, 0, 0, Duration(0.0)), Duration(6.0) },
		  { Time(1978, 1, 1, 0, 0, 0, Duration(0.0)), Duration(7.0) },
		  { Time(1979, 1, 1, 0, 0, 0, Duration(0.0)), Duration(8.0) },
		  { Time(1980, 1, 1, 0, 0, 0, Duration(0.0)), Duration(9.0) },
		  { Time(1981, 7, 1, 0, 0, 0, Duration(0.0)), Duration(10.0) },
		  { Time(1982, 7, 1, 0, 0, 0, Duration(0.0)), Duration(11.0) },
		  { Time(1983, 7, 1, 0, 0, 0, Duration(0.0)), Duration(12.0) },
		  { Time(1985, 7, 1, 0, 0, 0, Duration(0.0)), Duration(13.0) },
		  { Time(1988, 1, 1, 0, 0, 0, Duration(0.0)), Duration(14.0) },
		  { Time(1990, 1, 1, 0, 0, 0, Duration(0.0)), Duration(15.0) },
		  { Time(1991, 1, 1, 0, 0, 0, Duration(0.0)), Duration(16.0) },
		  { Time(1992, 7, 1, 0, 0, 0, Duration(0.0)), Duration(17.0) },
		  { Time(1993, 7, 1, 0, 0, 0, Duration(0.0)), Duration(18.0) },
		  { Time(1994, 7, 1, 0, 0, 0, Duration(0.0)), Duration(19.0) },
		  { Time(1996, 1, 1, 0, 0, 0, Duration(0.0)), Duration(20.0) },
		  { Time(1997, 7, 1, 0, 0, 0, Duration(0.0)), Duration(21.0) },
		  { Time(1999, 1, 1, 0, 0, 0, Duration(0.0)), Duration(22.0) },
		  { Time(2006, 1, 1, 0, 0, 0, Duration(0.0)), Duration(23.0) },
		  { Time(2009, 1, 1, 0, 0, 0, Duration(0.0)), Duration(24.0) },
		  { Time(2012, 7, 1, 0, 0, 0, Duration(0.0)), Duration(25.0) },
		  { Time(2015, 7, 1, 0, 0, 0, Duration(0.0)), Duration(26.0) },
		  { Time(2017, 1, 1, 0, 0, 0, Duration(0.0)), Duration(27.0) }
  };

  Time::Time() :
	 time(std::chrono::system_clock::now()),
	 timescale(UTC) {
  }

  Time::Time(const TimePointType & chronoTimePoint, Time::Timescale timetype) :
     time(chronoTimePoint),
     timescale(timetype){
  }

  Time::Time(int year, int mon, int day, int hour, int min, int sec, const Duration& fractionalSeconds, Time::Timescale timetype) {
	  struct std::tm t;
	  t.tm_sec = sec;
	  t.tm_min = min;
	  t.tm_hour = hour;
	  t.tm_mday = day;
	  t.tm_mon = mon-1;
	  t.tm_year = year - 1900;
	  t.tm_isdst = -1;
	  std::time_t tt = std::mktime(&t);

	  if (tt == -1) {
		  std::ostringstream oss;
		  oss << "Invalid Time args: year= " << year << " month= " << mon << " day= " << day <<
				  " hour= " << hour << " min= " << min << " sec= " << sec << "." << fractionalSeconds.asSeconds();
		  throw std::logic_error(oss.str());
	  }

	  time = std::chrono::system_clock::from_time_t(tt) + fractionalSeconds.timeDuration;
	  timescale = timetype;
  }

  const Time::TimePointType & Time::getTimePoint() const {
	 return time;
  }

  const std::string Time::getFormattedTimeString() {
	  std::time_t t = std::chrono::system_clock::to_time_t(time);
	  return std::ctime(&t);
  }

  Duration Time::getLeapSeconds(const Time& t) {
     LeapSecondsToDateMap::iterator lsI = Time::leapSecondsToDateTable.upper_bound(t);
     if (lsI != Time::leapSecondsToDateTable.begin()) {
        lsI--;
     }
     return (*lsI).second;
  }

  Time& Time::operator= (const Time &rhs) {
     if (this != &rhs) {
        time = rhs.time;
     }

     return *this;
  }

  //Time& Time::operator+= ( const Duration& rhs ) {
  //   time += rhs.timeDuration;
  //   return *this;
  //}

  //Time& Time::operator-= ( const Duration& rhs ) {
  //   time -= rhs.timeDuration;
  //   return *this;
  //}

  Duration Time::operator- ( const Time& rhs ) const {
     Duration d;
     d.timeDuration = time - rhs.time;
     return d;
  }

  Time Time::operator+ ( const Duration& rhs ) const {
     Time t ( time + rhs.timeDuration);
     return t;
  }

  Time Time::operator- ( const Duration& rhs ) const {
     Time t ( time - rhs.timeDuration);
     return t;
  }

  bool Time::operator== ( const Time &rhs ) const {
     return time == rhs.time;
  }

  bool Time::operator< ( const Time &rhs ) const {
     return time < rhs.time;
  }


  bool Time::operator!= (const Time &rhs) const {
     return ! (*this == rhs);
  }

  bool Time::operator> (const Time& rhs) const {
     return rhs < *this;
  }

  bool Time::operator<= (const Time& rhs) const {
     return ! (rhs < *this);
  }

  bool Time::operator>= (const Time& rhs) const {
     return ! (*this < rhs);
  }
}

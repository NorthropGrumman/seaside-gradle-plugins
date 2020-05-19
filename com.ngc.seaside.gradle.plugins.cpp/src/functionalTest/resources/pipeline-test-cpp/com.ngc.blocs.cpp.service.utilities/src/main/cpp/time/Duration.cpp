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
#include "time/Duration.h"

namespace blocs {

  Duration::Duration() {
	 timeDuration = DurationType::zero();
  }

  Duration::Duration(DurationType & duration) {
	 timeDuration = std::chrono::duration_cast<DurationType>(duration);
  }

  Duration::Duration(double seconds) {
	 std::chrono::duration<double> durationAsSeconds(seconds);
	 timeDuration = std::chrono::duration_cast<DurationType>(durationAsSeconds);
  }

  long long Duration::asMicroseconds() const {
	 std::chrono::microseconds durationInDesiredUnits =
		std::chrono::duration_cast<std::chrono::microseconds> (timeDuration);
	 return durationInDesiredUnits.count();
  }

  double Duration::asSeconds() const {
	 std::chrono::duration<double> durationInDesiredUnits =
		   std::chrono::duration_cast<std::chrono::duration<double> > (timeDuration);
	 return durationInDesiredUnits.count();
  }

  bool Duration::operator== (const Duration &rhs) const {
     return timeDuration == rhs.timeDuration;
  }

  bool Duration::operator< (const Duration &rhs) const {
     return timeDuration < rhs.timeDuration;
  }

  Duration Duration::operator/ (const Duration& divisor) const {
     return Duration (asSeconds() / divisor.asSeconds());
  }

  Duration Duration::operator/ (const double& divisor) const {
     return Duration (asSeconds() / divisor);
  }

  Duration& Duration::operator/= (const Duration& divisor) {
     return *this /= divisor.asSeconds();
  }

  //TODO
//  Duration& Duration::operator/= (const double& divisor) {
//     double quotient = asSeconds() / divisor;
//     if (quotient < 0.0) {
//        quotient -= DURATION_ROUNDOFF_FACTOR;
//     }
//     else if (quotient > 0.0) {
//        quotient += DURATION_ROUNDOFF_FACTOR;
//     }
//     long seconds = (long)quotient;
//     long count = (long)((quotient - seconds) * DURATION_TICKS_PER_SECOND);
//
//     timeDuration = boost::posix_time::time_duration (0, 0, seconds, count);
//     return *this;
//  }

  Duration Duration::operator* (const Duration& factor) const {
     return Duration ( asSeconds() * factor.asSeconds() );
  }

  Duration Duration::operator* (const double& factor) const {
     return Duration ( asSeconds() * factor );
  }

  Duration Duration::operator*= (const Duration& factor) {
     return *this *= factor.asSeconds();
  }
//TODO
//  Duration& Duration::operator*= (const double& factor) {
//     double product = asSeconds() * factor;
//     if (product < 0.0) {
//        product -= DURATION_ROUNDOFF_FACTOR;
//     }
//     else if (product > 0.0) {
//        product += DURATION_ROUNDOFF_FACTOR;
//     }
//     long seconds = (long)product;
//     long count = (long)((product - seconds) * DURATION_TICKS_PER_SECOND);
//
//     duration = boost::posix_time::time_duration (0, 0, seconds, count);
//     return *this;
//  }

  Duration Duration::operator- (const Duration& rhs) const {
     Duration d = (asSeconds() - rhs.asSeconds());
     return d;
  }

  Duration& Duration::operator-= (const Duration& rhs) {
	  timeDuration -= rhs.timeDuration;
     return *this;
  }

  Duration Duration::operator+ (const Duration& rhs) const {
     Duration d = (asSeconds() + rhs.asSeconds());
     return d;
  }

  Duration& Duration::operator+= (const Duration& rhs) {
	  timeDuration += rhs.timeDuration;
     return *this;
  }

  Duration Duration::operator-() {
     Duration d(-asSeconds());
     return d;
  }

  Duration operator*(const double& s, const Duration& d) {
     return d.operator*(s);
  }
}

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
//
//  Copyright 2017 Northrop Grumman Systems Corporation.
//------------------------------------------------------------------------------
#ifndef _BLOCS_Duration_h
#define _BLOCS_Duration_h

#include <chrono>
#include <ctime>
#include <iostream>

namespace blocs {

   /**
    * Wraps chrono duration to add support for units in input and conversion to
    * and from seconds as a double.
    */
   class Duration {

       /**
        * Friend so that the Time class has direct access to the time_duration values
        * @todo refactor this class so that this friend is no longer needed.
        */

       friend class Time;

   public:
      
      typedef std::chrono::system_clock::duration DurationType;

      /**
       * Default constructor
       */
      Duration();

      /**
       * Copy constructor
       * @param the duration to copy
       */
      Duration(DurationType & duration);

      /**
       * Constructs a duration based on seconds
       * @param seconds the duration in seconds
       */
      Duration(double seconds) ;

      /**
       * Returns the duration in microseconds
       */
      long long asMicroseconds() const;

      /**
       * Returns the duration as seconds
       */
      double asSeconds() const;

      /**
	  * Implements the operator== function that compares one Duration to
	  * another.  This operator will use the comparison precision value that
	  * is defined in TIME_COMPARE_PRECISION.
	  * @param rhs the Duration to compare to
	  * @return if *this == rhs
	  */
	 bool operator== (const Duration &rhs) const;

	 /**
	  * Implements the operator&lt; function that compares two Duration
	  * objects.  This operator will use the comparison precision value that
	  * is defined in TIME_COMPARE_PRECISION.
	  * @param rhs the Duration to comapre to
	  * @return if *this &lt; rhs
	  */
	 bool operator< (const Duration &rhs) const;

	 /**
	  * Implements the operator!= function that comapres two Duration objects.
	  * @param rhs the Duration to compare to
	  * @see Duration::operator==
	  * @return if *this != rhs
	  */
	 inline bool operator!= (const Duration &rhs) const;

	 /**
	  * Implements the operator&gt; function that compares two Duration objects.
	  * @param rhs the Duration to compare to
	  * @see Duration::operator<
	  * @return if *this &gt; rhs
	  */
	 inline bool operator> (const Duration &rhs) const;

	 /**
	  * Implements the operator&lt;= function that compares two Duration objects.
	  * @param rhs the Duration to compare to
	  * @see Duration::operator<
	  * @return if *this &lt;= rhs
	  */
	 inline bool operator<= (const Duration &rhs) const;

	 /**
	  * Implements the operator&gt;= function that compares two Duration objects.
	  * @param rhs the Duration to compare to
	  * @see Duration::operator<
	  * @return if *this &gt;= rhs
	  */
	 inline bool operator>= (const Duration &rhs) const;

	 /**
	  * Implements the operator/ function that divides two Duration objects.
	  * This function accomplishes this by converting both of the Duration
	  * objects to doubles and then dividing the doubles.
	  * @param divisor the Duration to divide this duration by
	  * @return a new Duration that is the quotient of the two Duration objects
	  */
	 Duration operator/(const Duration& divisor) const;

	 /**
	  * Implements the operator/ function that divides a Duration object by
	  * a double.  This function accomplishes this by converting this Duration
	  * to a double and then dividing by the parameter.
	  * @param divisor the double to divide by
	  * @return a new Duration that is the quotient of this Duration object
	  * and the provided double
	  */
	 Duration operator/(const double& divisor) const;

	 /**
	  * Implements the operator/= function that divides this Duration object
	  * by a double, saving the value in this Duration object and returning.
	  * @param divisor the divisor in the operation
	  * @return this object after the operation has occured
	  */
	 Duration& operator/=(const Duration& divisor);

//	 /**
//	  * Implements the operator/= function that divides this Duration object
//	  * by a double, saving the value in this Duration object and returning.
//	  * @param divisor the divisor in the operation
//	  * @return this object after the operation has occured
//	  */
//	 Duration& operator/=(const double& divisor);

	 /**
	  * Implements the operator* function that multiplies this Duration object
	  * by the provided factor and returns the product.
	  * @param factor the factor to multiply by
	  * @return a new duration containing the product of the two duration factors
	  */
	 Duration operator*(const Duration& factor) const;

	 /**
	  * Implements the operator* function that multiples this Duration object
	  * by a factor and returns the product
	  * @param factor the factor to multiply by
	  * @return a new duration containing the product of the two duration factors
	  */
	 Duration operator*(const double& factor) const;

	 /**
	  * Implements the operator*= function that multiplies this Duration object
	  * by the factor provided and returns the product
	  * @param factor the factor to multiply by
	  * @return this Duration after the factor has been applied
	  */
	 Duration operator*=(const Duration& factor);

//	 /**
//	  * Implements the operator*= function that multiplies this Duration object
//	  * by the factor provided and returns the product
//	  * @param factor the factor to multiply by
//	  * @return this Duration after the factor has been applied
//	  */
//	 Duration& operator*=(const double& factor);

	 /**
	  * Implements the operator- function that subtracts a Duration from this
	  * Duration and returns a new instance.
	  * @param rhs the value to subtract from this Duration
	  * @return a new instance containing the results of this operation
	  */
	 Duration operator-(const Duration& rhs) const;

	 /**
	  * Implements the operator-= function that subtracts a Duration from this
	  * Duration and this instance after the operation has taken place
	  * @param rhs the value to subtract from this Duration
	  * @return this instance after the operation has taken place
	  */
	 Duration& operator-=(const Duration& rhs);

	 /**
	  * Implements the operator- function that adds a Duration to this
	  * Duration and returns a new instance.
	  * @param rhs the value to add to this Duration
	  * @return a new instance containing the results of this operation
	  */
	 Duration operator+(const Duration& rhs) const;

	 /**
	  * Implements the unary - function that negates a Duration
	  * @return this instance after the operation has taken place
	  */
	 Duration operator-();

	 /**
	  * Implements the operator+= function that adds a Duration to this
	  * Duration and this instance after the operation has taken place
	  * @param rhs the value to add to this Duration
	  * @return this instance after the operation has taken place
	  */
	 Duration& operator+=(const Duration& rhs);

   protected:
      DurationType timeDuration;
   };

}

#endif

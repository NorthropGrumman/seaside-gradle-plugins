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
//------------------------------------------------------------------------------

#ifndef _BLOCS_Condition_H
#define _BLOCS_Condition_H

#include <chrono>
#include <condition_variable>

#include "threading/Mutex.h"
#include "time/Duration.h"
#include "time/Time.h"

namespace blocs {
      /**
      Condition class wraps a boost::condition object.
      */

      class Condition {

         public :

            Condition() : cond() {}

            void notifyOne() {
               cond.notify_one();
            }

            void notifyAll() {
               cond.notify_all();
            }

            void wait(ScopedLock& lock ) {
               cond.wait(lock ());
            }

            /*void wait(ScopedLockRecursive& lock ) {
               cond.wait(lock ());
            }*/

            bool timedWait(ScopedLock &lock, const Duration& waitFor)  {
               Time t = Time() + waitFor;
               /*boost::system_time t = boost::get_system_time() + boost::posix_time::seconds((long)waitFor.AsSeconds()) +
                                      boost::posix_time::microseconds( (long)((waitFor.AsSeconds() - (long)waitFor.AsSeconds()) * MICROSECONDS_PER_SECOND));
               */
               return (cond.wait_until(lock(), t.getTimePoint()) == std::cv_status::timeout);
            }

            /*
            bool timedWait(ScopedLockRecursive &lock, const std::chrono::milliseconds& waitFor)  {
               auto t = std::chrono::system_clock::now() + waitFor;
               // boost::system_time t = boost::get_system_time() +boost::posix_time::seconds((long)waitFor.AsSeconds()) +
               //                       boost::posix_time::microseconds( (long)((waitFor.AsSeconds() - (long)waitFor.AsSeconds()) * MICROSECONDS_PER_SECOND));

               return (cond.wait_until(lock(), t) == std::cv_status::timeout);
            }
            */

         private:
            Condition(const Condition& );  // implicit copyability does not make sense
            Condition &operator=(const Condition &); // implicit copyability does not make sense

            std::condition_variable cond;

      };

} //NAMESPACE


#endif


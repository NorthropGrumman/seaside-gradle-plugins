//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------

#ifndef _BLOCS_Condition_H
#define _BLOCS_Condition_H

#include <chrono>
#include <condition_variable>

#include "threading/Mutex.h"

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

            bool timedWait(ScopedLock &lock, const std::chrono::milliseconds& waitFor)  {
               auto t = std::chrono::system_clock::now() + waitFor;
               /*boost::system_time t = boost::get_system_time() + boost::posix_time::seconds((long)waitFor.AsSeconds()) +
                                      boost::posix_time::microseconds( (long)((waitFor.AsSeconds() - (long)waitFor.AsSeconds()) * MICROSECONDS_PER_SECOND));
               */
               return (cond.wait_until(lock(), t) == std::cv_status::timeout);
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


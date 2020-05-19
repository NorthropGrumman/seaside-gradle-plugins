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


#ifndef _BLOCS_Mutex_H
#define _BLOCS_Mutex_H

#include <mutex>
#include <memory>


namespace blocs {

      /**
      Mutex class implements a mutex.  This class wraps a boost::mutex.
      This mutex is non-recursive.  Any call to lock the mutex while it is
      already locked will be blocked, even if done from a single thread.  In fact,
      trying to lock the mutex on a single thread without unlocking it first will deadlock.
      */
      class Mutex {

         public :

            friend class ScopedLock;

            Mutex() : m() {}

         private:
            Mutex(const Mutex& );  // implicit copyability does not make sense
            Mutex &operator=(const Mutex &); // implicit copyability does not make sense

            std::mutex m;
      };

      /**
      MutexRecursive class implements a recursive mutex.  This class wraps a boost::recursive_mutex.
      This is a variant of mutex that incorporates a notion of ownership.  If a single thread
      attempts to acquire the mutex when it already has it, it will succeed.  However, this
      thread must unlock the mutex as many times as it acquires it.
      */
      class MutexRecursive {

         public :

            friend class ScopedLockRecursive;

            MutexRecursive() : m() {}

         private:
            MutexRecursive(const MutexRecursive& );  // implicit copyability does not make sense
            MutexRecursive &operator=(const MutexRecursive &); // implicit copyability does not make sense

            std::recursive_mutex m;
      };


      /** 
      ScopedLock implements a mutex lock that automatically releases the mutex when the object
      goes out of scope. This class wraps a std::unique_lock. 
      */
      class ScopedLock {

         public:

            friend class Condition;

            ScopedLock(Mutex &mx) {
               scopedLock = std::shared_ptr<std::unique_lock<std::mutex>>(
                  new std::unique_lock<std::mutex>(mx.m));
            }

            virtual ~ScopedLock() {
               scopedLock.reset();
            }

            void lock() {
               scopedLock->lock();
            }

            bool locked() {
               return scopedLock->owns_lock();
            }

            void unlock() {
               scopedLock->unlock();
            }

         private:
            std::shared_ptr<std::unique_lock<std::mutex>> scopedLock;

            std::unique_lock<std::mutex>& operator() () {
               return *scopedLock;
            }
      };


      /** 
      ScopedLockRecursive implements a mutex lock that automatically releases the recursive mutex when the object
      goes out of scope. This class wraps a boost::recursive_mutex::scoped_lock. 
      */
      class ScopedLockRecursive {

         public:

            friend class Condition;

            ScopedLockRecursive(MutexRecursive &mx) {
               scopedLock = std::shared_ptr<std::unique_lock<std::recursive_mutex>>(
                  new std::unique_lock<std::recursive_mutex>(mx.m));
            }

            virtual ~ScopedLockRecursive() {
               scopedLock.reset();
            }

            void lock() {
               scopedLock->lock();
            }

            bool locked() {
               return scopedLock->owns_lock();
            }

            void unlock() {
               scopedLock->unlock();
            }

         private:
            std::shared_ptr<std::unique_lock<std::recursive_mutex>> scopedLock;

            std::unique_lock<std::recursive_mutex>& operator() () {
               return *scopedLock;
            }
      };

} //NAMESPACE


#endif


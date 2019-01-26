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


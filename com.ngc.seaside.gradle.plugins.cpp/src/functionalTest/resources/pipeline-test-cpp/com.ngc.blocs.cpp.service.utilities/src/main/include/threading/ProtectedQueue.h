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
#ifndef _BLOCS_ProtectedQueue_H
#define _BLOCS_ProtectedQueue_H

#include <queue> 

#include "threading/Condition.h"
#include "threading/Mutex.h"
#include "time/Duration.h"


namespace blocs {

      /** \brief A ProtectedQueue of T objects that is protected by a mutex.
      \note typename T should not be a pointer to an object unless it is a smart pointer
      that can delete itself (but not an auto_ptr) or a memory leak will occur.
      */

      template <typename T>

      class ProtectedQueue {

         public :

            class Timeout : public std::range_error {
               public:
                  /**
                   * Constructor
                   */
                  Timeout () : std::range_error (std::string ("ProtectedQueue wait timeout occurred.")) {}
            };

            /** Constructor for the ProtectedQueue. */
            ProtectedQueue() {
               clear();
            }

            virtual ~ProtectedQueue() {
               clear();
            }

            /** Locks the mutex guard, then removes all the items from the container.*/
            void clear() {

               ScopedLock lock (guard);

               while(container.size() > 0) {
                  container.pop();
               }
            }

            /** Locks the mutex guard, then inserts the item and notifies
            the Condition that the insert is complete.
            Returns bool indicating whether top was changed as a result of the insert.
            */
            bool push(const T& item) {

               ScopedLock lock (guard);

               bool topChanged = container.empty();
               container.push(item);

               lock.unlock();

               //cond.notifyOne();
               cond.notifyAll();

               return topChanged;
            }

            /** Returns the top item in the container without removing it.
            Throws range_error if ProtectedQueue is empty.
            In a multi-threaded environment, be aware that what gets returned
            as the top is transient (if you call it again on the same thread, you
            may get a different top).
            */
            T top() {

               ScopedLock lock (guard);

               if (container.empty()) {
                  throw std::range_error("ProtectedQueue Empty");
               }

               T topItem = container.front();

               return topItem;
            }

            /** Returns a pair containing first: an indication of whether there are items in the queue
            and second: the top item in the container (if not empty) without removing it.
            In a multi-threaded environment, be aware that what gets returned
            as the top is transient (if you call it again on the same thread, you
            may get a different top).
            */
            std::pair<bool, T> peek() {

               ScopedLock lock (guard);

               if (container.empty()) {
                  T empty;
                  return std::pair<bool, T>(false, empty);
               }

               //T topItem = container.front();

               return std::pair<bool, T>(true, container.front());
            }

            /**
            Locks the container and returns the top item in the
            container and removes it from the container.
            Throws range_error if ProtectedQueue is empty. */
            T pop() {
               ScopedLock lock (guard);

               if (container.empty()) {
                  throw std::range_error("ProtectedQueue Empty");
               }

               T returnItem = container.front();
               container.pop();

               return returnItem;
            }

            /**
            Locks the container and returns a vector of all items in the
            container and removes them from the container.  Returns 
            an empty vector if there is nothing in the ProtectedQueue.*/
            std::vector<T> popAll() {
               ScopedLock lock (guard);

               std::vector<T> returnVector;
               while (!container.empty()) {
                  T returnItem = container.front();
                  returnVector.push_back(returnItem);
                  container.pop();
               }

               return returnVector;
            }

            /** Returns true if the container is empty. */
            bool isEmpty() const {
               ScopedLock lock (guard);
               return (container.size() == 0);
            }

            /** Returns the number of items in the container. */
            long size() const {
               ScopedLock lock (guard);
               return container.size();
            }


            /** This method will wait for the mutex to become unlocked,
            then pop the item off of the container and return it.
            */
            T waitForItem() {

               ScopedLock lock (guard);

               while (container.size() == 0) {
                  cond.wait(lock);
               }

               T returnItem = container.front();
               container.pop();

               return returnItem;
            }

            /** This method will wait for the mutex to become unlocked,
            then pop the item off of the container and return it.  If
            the timeout occurs prior to getting an item off the ProtectedQueue,
            then a ProtectedQueueTimeout exception will be thrown.
            */
            T waitForItem(const Duration &timeout) {

               ScopedLock lock (guard);

               while (container.size() == 0) {
                  // check if timeout occurred
                  if (!cond.timedWait(lock, timeout)) {
                     throw Timeout();
                  }
               }

               T returnItem = container.front();
               container.pop();

               return returnItem;

            }


         protected :

            typedef std::queue<T> ProtectedQueueContainer;

            ProtectedQueueContainer container;

            mutable Mutex guard;

            mutable Condition cond;

      };

} //NAMESPACE

#endif


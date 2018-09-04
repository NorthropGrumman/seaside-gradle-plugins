/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2018, Northrop Grumman Systems Corporation
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

#ifndef _BLOCS_ProtectedPriorityQueue_H
#define _BLOCS_ProtectedPriorityQueue_H

#include <set>  //for priority_queue
#include <vector>

#include "threading/Condition.h"
#include "threading/Mutex.h"

namespace blocs {


      /** \brief A queue of T objects that is protected by a mutex.
      \note typename T should not be a pointer to an object unless it is a smart pointer
      that can delete itself (but not an auto_ptr) or a memory leak will occur.
      */

      template <typename T>

      class ProtectedPriorityQueue {

         public :

            class Timeout : public std::range_error {
               public:
                  /**
                   * Constructor
                   */
                  Timeout () : std::range_error (std::string ("ProtectedPriorityQueue wait timeout occurred.")) {}
            };

            /** Constructor for the ProtectedPriorityQueue. */
            ProtectedPriorityQueue() {
               clear();
            }

            virtual ~ProtectedPriorityQueue() {
               clear();
            }

            /** Locks the mutex guard, then removes all the items from the container.*/
            void clear() {

               ScopedLock lock (guard);

               container.clear();
            }

            /** Locks the mutex guard, then inserts the item and notifies
            the Condition that the insert is complete.
            Returns bool indicating whether top was changed as a result of the insert.
            */
            bool push(const T& item) {

               ScopedLock lock (guard);

               std::pair<typename QueueContainer::iterator, bool> insertStatus(container.insert(item));
               unsigned long pos = std::distance( container.begin(), insertStatus.first );

               lock.unlock();

               //cond.notifyOne();
               cond.notifyAll();

               return (pos == 0);
            }

            /** Returns the top item in the container without removing it.
            Throws range_error if queue is empty.
            In a multi-threaded environment, be aware that what gets returned
            as the top is transient (if you call it again on the same thread, you
            may get a different top).
            */
            T top() {

               ScopedLock lock (guard);

               if (container.empty()) {
                  throw std::range_error("ProtectedPriorityQueue Empty");
               }

               T topItem = *container.begin();

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

               //T topItem = *container.begin();

               return std::pair<bool, T>(true, *container.begin());
            }

            /**
            Locks the container and returns the top item in the
            container and removes it from the container.
            Throws range_error if queue is empty. */
            T pop() {
               ScopedLock lock (guard);

               if (container.empty()) {
                  throw std::range_error("ProtectedPriorityQueue Empty");
               }

               T returnItem = *container.begin();

               container.erase(container.begin());

               //cond.notifyOne();
               //cond.notifyAll();

               return returnItem;
            }

            /**
            Locks the container and returns a vector of all items in the
            container and removes them from the container.  Returns 
            an empty vector if there is nothing in the queue.*/
            std::vector<T> popAll() {
               ScopedLock lock (guard);

               std::vector<T> returnVector;
               while (!container.empty()) {
                  T returnItem = *container.begin();
                  returnVector.push_back(returnItem);
                  container.erase(container.begin());
               }

               return returnVector;
            }

            /** Locks the mutex guard, then inserts the items and notifies
            the Condition that the insert is complete.
            Returns bool indicating whether top was changed as a result of the insert.
            */
            bool pushAll(const std::vector<T>& item) {

               ScopedLock lock (guard);

               std::pair<typename QueueContainer::iterator, bool> insertStatus;
               
               for (typename std::vector<T>::const_iterator i = item.begin(); i != item.end(); i++) {
                  insertStatus = container.insert((*i));
               }

               unsigned long pos = std::distance( container.begin(), insertStatus.first );

               lock.unlock();

               //cond.notifyOne();
               cond.notifyAll();

               return (pos == 0);
            }

            /** Returns true if the container is empty. */
            bool isEmpty() const {
               ScopedLock lock (guard);
               return container.empty();
            }

            /** Returns the number of items in the container. */
            long size() const {
               ScopedLock lock (guard);
               return container.size();
            }

            /** Removes an item from the container. */
            void remove(const T & item) {
               ScopedLock lock (guard);
               container.erase(item);
            }

            /** This method will wait for the mutex to become unlocked,
            then pop the item off of the container and return it.
            */
            T waitForItem() {

               ScopedLock lock (guard);

               while (container.empty()) {
                  cond.wait(lock);
               }

               T returnItem = *container.begin();

               container.erase(container.begin());
               return returnItem;
            }

            /** This method will wait for the mutex to become unlocked,
            then pop the item off of the container and return it.  If
            the timeout occurs prior to getting an item off the queue,
            then a ProtectedPriorityQueueTimeout exception will be thrown.
            */
            T waitForItem(const std::chrono::milliseconds& timeout) {

               ScopedLock lock (guard);

               while (container.empty()) {
                  // check if timeout occurred
                  if (!cond.timedWait(lock, timeout)) {
                     throw Timeout();
                  }
               }

               T returnItem = *container.begin();

               container.erase(container.begin());
               return returnItem;

            }


         protected :

            typedef std::set
            <T, std::less<T> > QueueContainer;

            QueueContainer container;

            mutable Mutex guard;

            mutable Condition cond;

      };

} //NAMESPACE

#endif


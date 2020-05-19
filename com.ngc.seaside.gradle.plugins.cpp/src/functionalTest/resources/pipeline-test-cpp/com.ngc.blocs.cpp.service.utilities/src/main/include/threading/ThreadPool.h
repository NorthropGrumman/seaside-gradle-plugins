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
#ifndef _BLOCS_ThreadPool_H
#define _BLOCS_ThreadPool_H

#include <iostream>
#include <list>
#include <vector>

#include "IThreadable.h"
#include "threading/Condition.h"
#include "threading/Mutex.h"
#include "threading/Threader.h"
#include "time/Duration.h"

namespace blocs {

      // See http://www.devguy.com/bb/viewtopic.php?p=1039 for the basis for
      // this class.

      class ThreadPool : public IThreadable {

         public:

            ThreadPool() {
               maxThreads = 0;
               queueSize = 0;
               deleteThreadable = false;
               init();
            }

            explicit ThreadPool(
               unsigned int nThreads,
               unsigned int nQueueSize = 0,
               bool deleteThreadableWhenDone = false) :
                  maxThreads(nThreads),
                  queueSize(nQueueSize),
                  deleteThreadable(deleteThreadableWhenDone) {

               assert(maxThreads > 0);
               assert(queueSize >= 0);

               init();

               if (queueSize == 0) {
                  queueSize = maxThreads * 2;
               }
            }

            void setQueueSize(unsigned int x) {
               {
            	   ScopedLock lock1(poolDataMutex);
            	   queueSize = x;
               }

               needThread.notifyAll();
            }

            unsigned int getQueueSize() const {
               return queueSize;
            }

            void setMaxThreads(unsigned int x) {
               {
                  ScopedLock lock1(poolDataMutex);
                  maxThreads = x;
               }

               needThread.notifyAll();
            }

            unsigned int getMaxThreads() const {
               return maxThreads;
            }

            unsigned int getNumThreadsAllocated() const {
               return numThreadsAllocated;
            }

            unsigned int getNumThreadsActive() const {
               return numThreadsActive;
            }

            // "ThreadCreatedListener" functionality not used at this time
            // PROBABLY NOT NEEDED NOW, SO COMMENTED OUT (PJS 8/13/08)
            /*
            void setThreadCreatedListener(const Functor1_T &f)
            {
            m_threadCreated = f;
            }
            const Functor1_T & getThreadCreatedListener() const
            {
            return m_threadCreated;
            }
            void clearThreadCreatedListener()
            {
            m_threadCreated = NoOp();
            }
            */

            /**
             * Get the number of requests that are waiting to be serviced
             * and are being serviced
             */
            unsigned int getRequestCount() const {
               ScopedLock lock1(poolDataMutex);
               return waitingThreadables.size();
            }

            bool isBusy() const {
               return busy;
            }

            // Call a function in a separate thread managed by the pool
            std::vector<long> invoke(IThreadable *threadableObj, const std::string& threadName = "") {

               std::vector<long> invokeState(6);
               //0 = num pool threads active/running
               //1 = num pool threads that have been allocated/created
               //2 = max poll threads
               //3 = num threadables waiting in the queue for a pool thread
               //4 = max waiting threadable queue size
               //5 = indicator of whether the invoke failed due to thread pool queue overflow (0=ok, 1=overflow)

               ScopedLock lock1(poolDataMutex);
               assert(maxThreads > 0);
               assert(queueSize >= numThreadsAllocated);
               invokeState[0] = numThreadsActive;
               invokeState[1] = numThreadsAllocated;
               invokeState[2] = maxThreads;
               invokeState[3] = waitingThreadables.size() - numThreadsActive;
               invokeState[4] = queueSize;
               invokeState[5] = 0;

               for (;;) {
                  if (stoppingFlag || stoppedFlag) break;
                  assert(!stoppingFlag);
                  assert(!denyInvokeFlag);
                  assert(!stoppedFlag);
                  assert(!numGeneralErrors);

                  try {

                     if (!blockInvokeFlag) {

                        //std::cout << waitingThreadables.size() << std::endl;
                        if (waitingThreadables.size() < numThreadsAllocated) {
                           // Don't create a thread unless it's needed.  There
                           // is a thread available to service this request.
                           addThreadable(threadableObj, threadName);
                           invokeState[0] = numThreadsActive;
                           invokeState[3] = waitingThreadables.size() - numThreadsActive;
                           lock1.unlock();
                           break;
                        }

                        bool added = false;

                        if (  (queueSize == 0) ||
                              (waitingThreadables.size() < (queueSize + numThreadsActive))) {
                           addThreadable(threadableObj, threadName);
                           added = true;
                        }

                        // have to create a thread
                        if (added && (numThreadsAllocated < maxThreads)) {

                           ++numThreadsAllocated;
                           invokeState[0] = numThreadsActive;
                           invokeState[1] = numThreadsAllocated;
                           invokeState[3] = waitingThreadables.size() - numThreadsActive;

                           lock1.unlock();
                           //ANR-std::pair<Threader *, long> threaderAndSizePair =
                           poolThreads.createThreader(this, threadName);
                           //numThreadsAllocated = threaderAndSizePair.second;
                           //invokeState[1] = numThreadsAllocated;


                           break;
                        }

                        if (added) {
                           invokeState[0] = numThreadsActive;
                           invokeState[3] = waitingThreadables.size() - numThreadsActive;
                           lock1.unlock();
                           break;
                        }
                        else {
                           std::cerr << "THREAD POOL OVERFLOW!!  invoke() failed for Threadable and it has been discarded." << std::endl;
                           invokeState[5] = 1;
                           lock1.unlock();
                           break;
                        }
                     }

                     try {
                        threadAvailable.wait(lock1);
                     }
                     catch (const Threader::Interrupted & ) {
                        if (!lock1.locked()) {
                           lock1.lock();
                        }
                     }
                     catch (...) {
                        std::cerr << "caught ... threadAvailable.wait(lock1) : THREAD POOL OVERFLOW" << std::endl;
                        throw;
                     }
                  }
                  catch (const std::exception & e) {
                     if (!lock1.locked()) {
                        lock1.lock();
                     }

                     std::cerr << e.what() << std::endl;

                     ++numGeneralErrors;
                     throw;
                  }
                  catch (...) {
                     if (!lock1.locked()) {
                        lock1.lock();
                     }

                     ++numGeneralErrors;

                     throw;
                  }
               }

               needThread.notifyAll();

               return invokeState;
            }

            virtual ~ThreadPool() {
               // Destructor should only run when there are no other users..
               // Let's say that other threads are calling stop() and blocked...
               // If the destructor notifies, unlocks... then the other threads
               // lock and unlock...  The attempt to lock will fail because the lock object was
               // destroyed when this destructor exits.
               // The destructor can not run while other threads are calling stop...
               // If you unlock and notify...  the same thing will happen.. because
               // the notify will cause stop() in other threads to attempt to
               // acquire the lock.  The problem is with stop() returning from the
               // destructor because that destroys the lock object.
               try {
                  stop();
               }
               catch (...) {
                  std::cerr << "ThreadPool destructor caught... ignoring" << std::endl;
               }
            }

            /**
             * Returns true if the thread poool is stopping e.g., it's in destructor or stop()
             */
            bool isStopping() const {
               return stoppingFlag;
            }

            /**
             * Returns when this object is stopping or has been stopped
             *
             * @waitMilliseconds - 0 to wait forever
             */
            bool waitUntilStopping(int waitMilliseconds = 0) {

               ScopedLock lock1(poolDataMutex);

               if (stoppingFlag || stoppedFlag) {
                  return true;
               }

               if (waitMilliseconds > 0) {
                  stopping.timedWait(lock1, Duration(waitMilliseconds / 1000.0));
               }
               else {
                  stopping.wait(lock1);
               }

               return stoppingFlag || stoppedFlag;
            }

            /**
             * Make code that calls waitUntilStopping to wake up
             */
            void triggerStoppingCondition() {
               stopping.notifyAll();
            }

            /**
             * Returns true if the thread poool is stopping e.g., it's in destructor or stop()
             */
            bool isStopped() const {
               return stoppedFlag;
            }

            /**
             * Stop all threads.
             * An exception is thrown if invoke() is called.
             * There is no "un-do" method.
             */
            void stop(bool processQueuedRequests = false) {
               { //scoping for lock
                  ScopedLock lock1(poolDataMutex);

                  if (stoppedFlag) {
                     return;
                  }

                  if (stoppingFlag) {
                     while (!stoppedFlag) {
                        allStopped.wait(lock1);
                     }

                     return;
                  }

                  stoppingFlag = true;
               }

               stopping.notifyAll();

               try {
                  blockInvokeFlag = false;
                  denyInvokeFlag = true;

                  if (processQueuedRequests) {
                     wait();
                  }

                  { //scoping for lock
                     ScopedLock lock1(poolDataMutex);
                     stopFlag = true;
                  }

                  unpause();

                  poolThreads.joinAll();

                  { //scoping for lock
                     ScopedLock lock1(poolDataMutex);

                     // This shouldn't be necessary but what the heck..
                     //assert(waitingThreadables.empty());

                     stoppingFlag = false;
                     stoppedFlag = true;
                     stopFlag = false;
                     numThreadsActive = 0;
                  }

                  allStopped.notifyAll();
               }
               catch (...) {
                  std::cerr << "ThreadPool.stop() caught ... " << std::endl;
                  ScopedLock lock1(poolDataMutex);
                  ++numGeneralErrors;
                  stoppingFlag = false;
                  stoppedFlag = true;
                  allStopped.notifyAll();
               }
            }

            /**
             * When the pause property is enabled the background thread stops processing requests.
             * Requests continue to be queued.
             *
             * Calling pause isn't exactly thread-safe, because pause(a) and pause(!a) are
             * not serialized with other threads that may be doing the same thing but using
             * opposite values of a.  Also, wait() calls unpause().
             */
            void pause() {
               pauseFlag = true;
            }

            void unpause(bool bPause = true) {
               pauseFlag = false;
               needThread.notifyAll();
            }

            /**
             * Get the pause property
                */
            bool isPaused() const {
               return pauseFlag;
            }

            /**
             * When the blockInvoke property is enabled, invoke() blocks
             *
             * Calling blockInvoke isn't exactly thread-safe, because
             * blockInvoke(a) and blockInvoke(!a) are
             * not serialized with other threads that may be doing the same thing but using
             * opposite values of a.  Furthermore, wait() might set blockInvoke to false.
             */
            void setBlockInvoke(bool bBlock = true) {
               blockInvokeFlag = bBlock;

               if (!bBlock) {
                  threadAvailable.notifyAll();
               }
            }

            /**
            * Get the blockInvoke property
             */
            bool getBlockInvoke() const {
               return blockInvokeFlag;
            }

            /**
             * Wait until all queued requests have been processed.  Nothing is actually
             * guaranteed after wait() finishes.  There may be items in the queue to
             * process if invoke() is called by another thread while wait() is running,
             * or if invoke() is called immediately after wait() finishes.
             *
             * Sets the "pause" property to false (otherwise, this method would hang
             * for ever).
             *
             * To prevent additional items from being queued, call stop(true)
             *
             * @param bBlockInvoke - If true, all calls to invoke() are blocked until all pending requests
             *   are processed. If true, sets this property to false when exiting.
             * @param bDenyInvoke - If true, all calls to invoke() are responded to by throwing an exception.
             *   If true, sets this property to false when exiting.
             */
            void wait(bool bBlockInvoke = true, bool bDenyInvoke = false) {
               if (bBlockInvoke) {
                  blockInvokeFlag = true;
               }

               if (bDenyInvoke) {
                  denyInvokeFlag = true;
               }

               unpause();

               try {
                  ScopedLock lock1(poolDataMutex);

                  while (!waitingThreadables.empty()) {
                     try { threadAvailable.wait(lock1); }
                     catch(const Threader::Interrupted &) {}
                  }
               }
               catch (...) {
                  {
                     ScopedLock lock1(poolDataMutex);
                     ++numGeneralErrors;
                  }

                  if (bBlockInvoke) {
                     blockInvokeFlag = false;
                  }

                  if (bDenyInvoke)
                     denyInvokeFlag = false;

                  throw;
               }

               if (bBlockInvoke) {
                  blockInvokeFlag = false;
               }

               if (bDenyInvoke) {
                  denyInvokeFlag = false;
               }
            }

         private:

            ThreadPool(const ThreadPool&);
            ThreadPool& operator = (const ThreadPool&);

            typedef std::pair<IThreadable *, std::string> ThreadableNamePair;
            typedef std::list<ThreadableNamePair> ThreadableContainer;

            ThreadableContainer waitingThreadables;  //m_functorQueue
            ThreadableContainer::iterator nextThreadable;

            unsigned int numThreadsAllocated;  //m_nThreadsCreated
            unsigned int numThreadsActive;     //m_nThreadsCreateInUse
            unsigned int maxThreads;           //m_nMaxThreads
            unsigned int queueSize;            //m_nMaxWaiters
            unsigned int numGeneralErrors;     //m_nGeneralErrors
            unsigned int numThreadableErrors;  //m_nFunctorErrors
            bool busy;

            /** Mutex protection for the pool data members. */
            mutable Mutex poolDataMutex;

            /** Condition that is triggered when a thread is available. */
            Condition  threadAvailable;  //m_threadAvailable

            /** Condition that is triggered when a thread is needed. */
            Condition  needThread;       //m_needThread

            /**  */
            Condition  allStopped;       //m_allStopped

            /**  */
            Condition  stopping;         //m_stopping

            /** Thread group containing the pool of Threader objects. */
            ThreaderGroup poolThreads;

            /** Flag to indicate that the pool has been stopped. */
            bool pauseFlag;        //m_bPause
            bool stopFlag;         //m_bStop
            bool stoppingFlag;     //m_bStopping
            bool stoppedFlag;      //m_bStopped
            bool denyInvokeFlag;   //m_bDenyInvoke
            bool blockInvokeFlag;  //m_bBlockInvoke


            /** Flag to indicate that the pool should clean up the threadable once the execution is complete. */
            bool deleteThreadable;


            void init() {

               numThreadsAllocated = 0;
               numThreadsActive = 0;
               //maxThreads = 0; //set in the constructor initializer
               //queueSize = 0; //set in the constructor initializer
               numGeneralErrors = 0;
               numThreadableErrors = 0;

               pauseFlag = false;
               stopFlag = false;
               stoppingFlag = false;
               stoppedFlag = false;
               denyInvokeFlag = false;
               blockInvokeFlag = false;

               nextThreadable = waitingThreadables.end();
               busy = false;
            }


            void addThreadable(IThreadable *threadable, const std::string& threadName) {   //addFunctor
               bool bAtEnd = false;

               if (nextThreadable == waitingThreadables.end())
                  bAtEnd = true;

               waitingThreadables.push_back(std::make_pair(threadable, threadName));
               busy = true;

               if (bAtEnd) {
                  --nextThreadable;
               }
            }


            // Thread entry point.  This method runs once per thread.
            void execute(Threader *threader) {  //beginThread
               try {
                  ScopedLock lock1(poolDataMutex);

                  for (;;) {
                     if (stopFlag) {
                        break;
                     }

                     if (pauseFlag || (nextThreadable == waitingThreadables.end())) {
                        // Wait until someone needs a thread
                        needThread.wait(lock1);
                     }
                     else {
                        ++numThreadsActive;

                        try {
                           ThreadableContainer::iterator iter = nextThreadable;
                           IThreadable *obj = (*iter).first; // waitingThreadables.front();
                           std::string threadName = (*iter).second;
                           ++nextThreadable;

                           lock1.unlock();

                           try {
                        	  threader->setName(threadName);
                              obj->execute(threader);

                              if (deleteThreadable) {
                            	  std::cout << "DELETING THREADABLE IN THREADPOOL" << std::endl;
                                 delete obj;
                                 obj = NULL;
                              }
                           }
                           catch (...) {
                              if (deleteThreadable && (obj != NULL)) {
                            	  std::cout << "DELETING THREADABLE IN THREADPOOL" << std::endl;
                                 delete obj;
                                 obj = NULL;
                              }

                              lock1.lock();

                              ++numThreadableErrors;
                              //waitingThreadables.erase(iter); //uncomment if we decide to throw this upward...
                              std::cerr << "ThreadPool call to Threadable->execute() threw an error which was consumed by the Pool." << std::endl;

                              lock1.unlock();
                           }

                           lock1.lock();

                           --numThreadsActive;
                           waitingThreadables.erase(iter);
                           busy = !waitingThreadables.empty();

                           lock1.unlock();
                           threadAvailable.notifyAll();
                           lock1.lock();
                        }
                        catch (...) {
                           if (!lock1.locked()) {
                              lock1.lock();
                           }

                           --numThreadsActive;

                           throw;
                        }
                     }
                  } //for (ever)
               }
               catch (...) {
                  ScopedLock lok1(poolDataMutex);
                  ++numGeneralErrors;
                  std::cerr << "ThreadPool execute encountered an error which was consumed by the Pool." << std::endl;
               }

               threadAvailable.notifyAll();
            }
      };

}

#endif


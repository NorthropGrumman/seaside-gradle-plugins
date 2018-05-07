//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------
#ifndef _BLOCS_Threader_H
#define _BLOCS_Threader_H

#include <cassert>

#include <thread>
#include <functional>
#include <memory>
#include <chrono>
#include <list>
#include <set>
#include <sstream>

#include "IThreadable.h"

#include "threading/Mutex.h"

#ifdef WIN32
#include "Windows.h"
#include "processthreadsapi.h"
#endif


namespace blocs {

      /**
      Threader class is a separate object that plays the role of a command
      processor, allowing alternative executor implementations, such as
      pooling or time-based events.  This class encapsulates the boost::thread
      implementation.
      */

      class Threader {

         public :
            class Interrupted : public std::exception {
               virtual const char* what() const throw()
               {
                  return "Thread Interrupt Exception";
               }
            };


            enum ThreadState {
               RUNNING,
               NOT_RUNNING
            };

            Threader(const std::string& _name = "") : state(NOT_RUNNING), name(_name) {}

            virtual ~Threader() {
            }

            void execute(IThreadable *threadableObj) {
               // create the thread and run it
               myThreadable = threadableObj;
               thread = std::shared_ptr<std::thread>(new std::thread(std::bind(&Threader::run, this)));
               std::ostringstream out;
               out << thread->get_id();
               id = out.str();
               

            }

            ThreadState getState() const {
               return state;
            }

            bool isRunning() const {
               return (state == RUNNING);
            }

            void interrupt() {
#ifdef WIN32
               //::TerminateThread(thread->native_handle(), 0);
               //throw Interrupted();
#else
               //pthread_cancel(thread->native_handle());
               //throw Interrupted();
#endif
               //thread->interrupt();
            }

            void join() {
               if (std::this_thread::get_id() == thread->get_id()) return;  //don't try to join with self
               thread->join();
            }

            const std::string & getId() const {
               return id;
            }

            const std::string & getName() const {
               return name;
            }

            void setName(const std::string& name) {
            	this->name = name;
            }

            static void yield() {
               std::this_thread::yield();
            }

            static void sleep(const std::chrono::milliseconds& t) {
               std::this_thread::sleep_for(t);
            }


         private:
            void* run() {
               assert(myThreadable);

               try {
                  state = RUNNING;
                  myThreadable->execute(this);
                  state = NOT_RUNNING;
               }
               catch (...) {
                  state = NOT_RUNNING;
                  throw;
               }

               return 0;
            }

            std::shared_ptr<std::thread> thread;
            IThreadable *myThreadable;

            ThreadState state;
            std::string id;
            std::string name;

      };

      class ThreaderGroup {

         public:
            ThreaderGroup() : threaders() {}

            virtual ~ThreaderGroup() {
               for (std::list<Threader*>::iterator it = threaders.begin();
                     it != threaders.end();
                     ++it) {
                  delete *it;
               }

               threaders.clear();
            }

            std::pair<Threader *, long> createThreader(IThreadable *threadable, const std::string& threadName = "") {
               ScopedLock guard(mutex);
               Threader * new_threader = new Threader(threadName);
               threaders.push_back(new_threader);
               std::pair<Threader *, long> threaderAndSizePair (new_threader, threaders.size());
               new_threader->execute(threadable);
               return threaderAndSizePair;
            }

            long getSize() const {
               ScopedLock guard(mutex);
               return threaders.size();
            }

            void joinAll() {
               ScopedLock guard(mutex);

               for (std::list<Threader*>::iterator it = threaders.begin();
                     it != threaders.end();
                     ++it) {
                  try {
                     //std::cout << "ThreadGroup.joinAll() joining thread " << (*it)->getId() << " " << (*it)->getState() << std::endl;
                     (*it)->join();
                  }
                  catch (...) {
                     //std::cerr << "ThreadGroup.joinAll() trapped ... on member thread join" << std::endl;
                  }
               }
            }
            
            void interruptAll() {
               ScopedLock guard(mutex);

               for (std::list<Threader*>::iterator it = threaders.begin();
                     it != threaders.end();
                     ++it) {
                  (*it)->interrupt();
               }
            }
            

         private:
            mutable Mutex mutex;
            std::list<Threader *> threaders;
      };

} //NAMESPACE


#endif


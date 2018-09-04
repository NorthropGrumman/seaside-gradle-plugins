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

#include <memory>

#include "gtest/gtest.h"
#include "threading/Mutex.h"
#include "threading/Condition.h"
#include "BasicThreadService.h"
#include "threading/Threader.h"

using namespace blocs;
using namespace blocs::basicthreadservice;

Mutex io_mutex;
const int BUF_SIZE = 10;

class ThreadServiceTestFixture : public ::testing::Test {
protected:

	std::shared_ptr<BasicThreadService> basicThreadService;
	size_t numThreads = 2;
	//size_t threadPoolId;


	virtual void SetUp() {

		basicThreadService = std::shared_ptr<BasicThreadService>(new BasicThreadService());
		basicThreadService->activate();
		//threadPoolId = basicThreadService->createThreadPool(numThreads);

	}

	virtual void TearDown() {

		//basicThreadService->deleteThreadPool(threadPoolId);
	}

};

class Buffer {

   public:
      //typedef boost::mutex::scoped_lock   scoped_lock;

	Buffer() : p(0), c(0), full(0) { }

      void put(int m) {

         ScopedLock lock (mutex)

         ;

         if (full == BUF_SIZE) {
            {

               ScopedLock lock (io_mutex)

               ;
               //std::cout << "Buffer is full. Waiting..." << std::endl;
            }

            while (full == BUF_SIZE)
               cond.wait(lock )

               ;
         }

         buf[p] = m;

         p = (p + 1) % BUF_SIZE;
         ++full;
         cond.notifyOne();
      }

      int get
      () {
         ScopedLock lk(mutex);

         if (full == 0) {
            {

               ScopedLock lock (io_mutex)

               ;
               //std::cout << "Buffer is empty. Waiting..." << std::endl;
            }

            while (full == 0)
               cond.wait(lk);
         }

         int i = buf[c];

         c = (c + 1) % BUF_SIZE;
         --full;
         cond.notifyOne();
         return i;
      }

   private:
      Mutex mutex;
      Condition cond;
      unsigned int p, c, full;
      int buf[BUF_SIZE];
};

Buffer buf;

//class Writer : public Threadable {
class Writer : public IThreadable {
   public:
	int m_iter;

	  Writer(int m) : m_iter(m) {}
      void execute(Threader *threader) {
         for (int n = 0; n < m_iter; ++n) {
            buf.put(n);

            Threader::yield();
         }
      }
};



//class Reader : public Threadable {
class Reader : public IThreadable {

private:
	std::ostringstream &buffer;
	int n_iter;
   public:

	  Reader(int n, std::ostringstream &readBuf) : buffer(readBuf), n_iter(n){}

	  void execute(Threader *threader) {
         for (int x = 0; x < n_iter; ++x) {
            int n = buf.get();
            {
               //ScopedLock lock (io_mutex);

               buffer << n;
            }
         }
      }
};

class TrueExecutor : public IThreadable {
public:
	bool &myVal;

	TrueExecutor(bool &mutexLock) : myVal(mutexLock) {}

	void execute(Threader *threader) {
		myVal = true;
	}
};

class SleepTask : public IThreadable {
public:
	Mutex io_m;
	int &myCnt;

	SleepTask(int &cnt) : myCnt(cnt) {}

	void execute(Threader *threader) {
		ScopedLock l(io_m);
		myCnt++;
		//std::cout << "SleepTask Threader: " << threader->getName() << std::endl;
		threader->sleep(std::chrono::milliseconds(100));
	}
};

TEST_F(ThreadServiceTestFixture, testLongRunningThread) {
	int numVals = 5;
	std::ostringstream buf1;
	Reader reader(numVals, buf1);
	Writer writer(numVals);
	std::shared_ptr<Threader> thread1 = basicThreadService->submitLongLivingTask("thread1", &writer);
	std::shared_ptr<Threader> thread2 = basicThreadService->submitLongLivingTask("thread2", &reader);

	thread1->join();
	thread2->join();

	std::string result = buf1.str();
	EXPECT_TRUE(result == "01234");
}

TEST_F(ThreadServiceTestFixture, testThread) {
	bool mLock = false;
	TrueExecutor exec(mLock);

	basicThreadService->submit("thread1", &exec);

	while(!mLock) {

		std::this_thread::yield();
	}

	EXPECT_TRUE(mLock);
}

TEST_F(ThreadServiceTestFixture, testThreadPool) {

	int cntr = 0;
	SleepTask sleep(cntr);

	size_t poolID = basicThreadService->createThreadPool(5);

	// Note that the yeilds are just to let the tasks run
	basicThreadService->submit("1", &sleep, poolID);
	//std::this_thread::yield();
	basicThreadService->submit("2", &sleep, poolID);
	//std::this_thread::yield();
	basicThreadService->submit("3", &sleep, poolID);
	//std::this_thread::yield();
	basicThreadService->submit("4", &sleep, poolID);
	//std::this_thread::yield();
	basicThreadService->submit("5", &sleep, poolID);

    //std::this_thread::sleep_for(std::chrono::seconds(6));


	basicThreadService->deleteThreadPool(poolID, true);

	EXPECT_TRUE(cntr == 5);

}

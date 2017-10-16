//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------

#ifndef _BLOCS_TextFileOutputStrategy_H
#define _BLOCS_TextFileOutputStrategy_H

#include <memory>

#include "logger/LogOutputStrategy.h"
#include "threading/Threadable.h"
#include "threading/Threader.h"
#include "threading/ThreadPool.h"

using namespace blocs;

namespace blocs { namespace basiclogservice {

      /**
       * Implements a LogOutputStrategy that logs the data strings to
       *  an ASCII text file.
       */

      class TextFileOutputStrategy :
            public LogOutputStrategy {

         public :

            static void createTextFile(const std::string & fileName);

            static void writeTextToFile(const std::string & fileName, const std::string & textToWrite);


            class TextFileLogBuffer : public Threadable {
               public :
                  TextFileLogBuffer(const std::string & logFileName);
                  virtual ~TextFileLogBuffer();
                  void append(const std::string & bufferData);
                  bool snap();
                  virtual void execute(Threader *threader);
               private :
                  std::string fileName;
                  std::ostringstream textBuffer1;
                  std::ostringstream textBuffer2;
                  std::ostringstream * appendBuffer;
                  std::ostringstream * writeBuffer;
                  mutable Mutex textBufferMutex;
            };

            class TextFileAsynchronousLogWriter : public Threadable {

               public :
                  static Threader * logWriterThread;

                  TextFileAsynchronousLogWriter();

                  virtual ~TextFileAsynchronousLogWriter();

                  void setMaxWriteThreads(unsigned int max);

                  void registerBuffer(const std::string & bufferName, std::shared_ptr<TextFileLogBuffer> buffer);
                  void removeBuffer(const std::string & bufferName);

                  void execute(Threader *threader);
                  void stop();

               private :
                  volatile bool stopped;
                  ThreadPool * asynchWriteThreadPool;

                  std::map<std::string, std::shared_ptr<TextFileLogBuffer> > bufferMap;
                  mutable Mutex bufferMapMutex;

            };

            static TextFileAsynchronousLogWriter * textFileAsynchronosLogWriter;


            /**
             * Constructor
             * @param fileName the name of the file to open and write data to
             */
            TextFileOutputStrategy (const std::string& fileName, bool synchronous);

            virtual ~TextFileOutputStrategy();

            /* From LogOutputStrategy */
            virtual void initialize(const std::string& loggerName);

            /* From LogOutputStrategy */
            virtual void outputFormattedLogLine(const LogOutputData & logData);

            /* From LogOutputStrategy */
            virtual void outputRawLogLine(const std::string & logData);

            /* From LogOutputStrategy */
            virtual void shutdown(void);

         protected:

            /**
             * Writes the string provided to it to the log file that was opened
             * by this strategy.
             * @param logLine the data to write to the log file
             */
            void writeLogLine(const std::string& logLine);

         private :


            /**
             * The id of the logger
             */
            std::string loggerID;

            /**
             * The name of the file to be used by this logger
             */
            std::string fileName;

            std::shared_ptr<TextFileOutputStrategy::TextFileLogBuffer> textFileLogBufferSharedPtr;

            /**
             * bool indicating whether writes should be synchronous or asynchronous
             */
            bool synchronousWrite;

            /**
             * A mutex to protect against multiple simultaneous writes to
             * this strategy instance
             */
            mutable Mutex synchronousWriteMutex;

      };

}} //NAMESPACE

#endif

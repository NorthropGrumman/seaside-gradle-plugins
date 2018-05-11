//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------

#include "logger/TextFileOutputStrategy.h"
#include <string>
#include <fstream>
#include <iostream>
#include <limits>

#ifdef _WIN32
#include <windows.h>
#include <tchar.h>
#include <stdio.h>

#define FILE_WRITE_TO_END_OF_FILE       0xffffffff
#endif



namespace blocs { namespace basiclogservice {


      //statics
      TextFileOutputStrategy::TextFileAsynchronousLogWriter *
            TextFileOutputStrategy::textFileAsynchronosLogWriter = new TextFileAsynchronousLogWriter();
      Threader * TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread = NULL;



      //////////////////////////////////////////
      //
      // static Create/Write methods (unprotected)
      //
      /////////////////////////////////////////

      void TextFileOutputStrategy::createTextFile(
            const std::string & fileName) {

#ifdef _WIN32

         // "Tuned" for Windows using low level Windows API routines
         HANDLE hFile;
         hFile = CreateFile(fileName.c_str(),                // name of the write
                            GENERIC_WRITE,          // open for writing
                            FILE_SHARE_READ | FILE_SHARE_WRITE, // do not share
                            NULL,                   // default security
                            CREATE_ALWAYS,          // assume it is there
                            FILE_ATTRIBUTE_NORMAL,  // normal file
                            NULL);                  // no attr. template

         if (hFile == INVALID_HANDLE_VALUE) {
            std::cerr << "LOGCREATE: Could not create file: " << fileName << ", error=" << GetLastError();
            std::cerr << "  All log message writes attempted to this log file will fail." << std::endl;
            return;
         }

         CloseHandle(hFile);

         OVERLAPPED overlapped;
         ::memset(&overlapped, 0, sizeof(overlapped));
         overlapped.Offset = FILE_WRITE_TO_END_OF_FILE;
         overlapped.OffsetHigh = -1;

#else

         // Standard C++
         std::ofstream file (fileName.c_str(), std::ios::trunc);

         if (file.is_open() ) {
            file.close();
         }

#endif
      }

      void TextFileOutputStrategy::writeTextToFile(
            const std::string & fileName,
            const std::string & textToWrite) {

         try {

#ifdef _WIN32
            // "Tuned" for Windows using low level Windows API routines
            HANDLE hFile;

            hFile = CreateFile(fileName.c_str(),     // name of the write
                               FILE_APPEND_DATA | SYNCHRONIZE,          // open for writing
                               FILE_SHARE_READ | FILE_SHARE_WRITE, // allow shared access
                               NULL,                   // default security
                               OPEN_EXISTING,          // assume it is there
                               FILE_ATTRIBUTE_NORMAL,  // normal file
                               NULL);                  // no attr. template

            if (hFile == INVALID_HANDLE_VALUE) {
               //This is usually an indicator that the file was not successfully created
               //Which is generally because of "name to long".
               //std::cerr << "TextFileAsynchronousLogWriter: Could not open file: " << next.fileName << ", error=" << GetLastError() << std::endl;
            }
            else {

               DWORD dwBytesWritten;

               OVERLAPPED overlapped;
               ::memset(&overlapped, 0, sizeof(overlapped));
               overlapped.Offset = FILE_WRITE_TO_END_OF_FILE;
               overlapped.OffsetHigh = -1;

               if (!WriteFile(
                        hFile,                // open file handle
                        textToWrite.c_str(),  // start of data to write
                        textToWrite.length(), // number of bytes to write
                        &dwBytesWritten,      // number of bytes that were written
                        &overlapped)          // overlapped structure
                  ) {
                  std::cerr << "TextFileAsynchronousLogWriter: Could not write to file: " << fileName << ", error=" << GetLastError() << std::endl;
               }

               CloseHandle(hFile);
            }

#else

            // Standard C++
            std::ofstream file (fileName.c_str(), std::ios::app);

            if (file.is_open() ) {
               file << textToWrite;
               file.close();
            }

#endif
         }
         catch (const std::exception & e) {
            std::cerr << "TextFileOutputStrategy: Exception on write to: " << fileName << " text: " << textToWrite << " what: " << e.what() << std::endl;
         }
         catch (...) {
            std::cerr << "TextFileOutputStrategy: Caught ... on write to: " << fileName << " text: " << textToWrite << std::endl;
         }
      }


      //////////////////////////////////////////
      //
      // TextFileOutputStrategy
      //
      /////////////////////////////////////////
      TextFileOutputStrategy::TextFileOutputStrategy (
            const std::string & fileName,
            bool synchronous) :
                  fileName (fileName),
                  textFileLogBufferSharedPtr(std::shared_ptr<TextFileOutputStrategy::TextFileLogBuffer>()),
                  synchronousWrite (synchronous){

         createTextFile(fileName);
      }

      TextFileOutputStrategy::~TextFileOutputStrategy() {
         shutdown();
      }

      void TextFileOutputStrategy::initialize (const std::string & loggerName) {
         loggerID = loggerName;
         if (!synchronousWrite) {
            textFileLogBufferSharedPtr.reset(new TextFileOutputStrategy::TextFileLogBuffer(fileName));
            TextFileOutputStrategy::textFileAsynchronosLogWriter->registerBuffer(fileName, textFileLogBufferSharedPtr);
         }
      }

      void TextFileOutputStrategy::shutdown() {
         TextFileOutputStrategy::textFileAsynchronosLogWriter->removeBuffer(fileName);
      }

      void TextFileOutputStrategy::outputFormattedLogLine (const LogOutputData & logData) {
         std::ostringstream formatBuffer;
         formatLogLine(logData, formatBuffer);
         writeLogLine (formatBuffer.str());
      }

      void TextFileOutputStrategy::outputRawLogLine (const std::string & logData) {
         writeLogLine (std::string(logData));
      }

      void TextFileOutputStrategy::writeLogLine (const std::string & logLine) {

         //ASYNCHRONOUS WRITE
        if ((!synchronousWrite) && (TextFileOutputStrategy::textFileAsynchronosLogWriter != NULL)) {

           //put the logLine in the asynchronous buffer for later writing
            textFileLogBufferSharedPtr->append(logLine);
         }

         //SYNCHRONOUS WRITE
         else {
            ScopedLock lock(synchronousWriteMutex);

            //immediately write the logLine
            writeTextToFile(fileName, logLine);

         } //else do synchronous write

      }


      //////////////////////////////////////////
      //
      // TextFileLogBuffer
      //
      /////////////////////////////////////////
      TextFileOutputStrategy::TextFileLogBuffer::TextFileLogBuffer(
            const std::string & logFileName) :
                  fileName(logFileName),
                  textBuffer1(),
                  textBuffer2(),
                  appendBuffer(&textBuffer1),
                  writeBuffer(&textBuffer2) {
         appendBuffer->clear();
         appendBuffer->str("");
         writeBuffer->clear();
         writeBuffer->str("");
      }

      TextFileOutputStrategy::TextFileLogBuffer::~TextFileLogBuffer() {
         bool dataToWrite = snap();
         if (dataToWrite) {
            execute(NULL);
         }
         appendBuffer->str("");
         writeBuffer->str("");
      }

      void TextFileOutputStrategy::TextFileLogBuffer::append(const std::string & bufferData) {
         ScopedLock lock(textBufferMutex);
         (*appendBuffer) << bufferData;
      }

      bool TextFileOutputStrategy::TextFileLogBuffer::snap() {
         ScopedLock lock(textBufferMutex);

         //if there is nothing in the append stream, do nothing and return false
         if (appendBuffer->tellp() == std::ostream::pos_type(0)) return false;

         writeBuffer = appendBuffer;
         if (appendBuffer == &textBuffer1) {
            appendBuffer = &textBuffer2;
         }
         else {
            appendBuffer = &textBuffer1;
         }
         appendBuffer->clear();
         appendBuffer->str("");

         return true;
      }

      void TextFileOutputStrategy::TextFileLogBuffer::execute(Threader *threader) {
         writeTextToFile(fileName, writeBuffer->str());
         writeBuffer->clear();
         writeBuffer->str("");
      }

      //////////////////////////////////////////
      //
      // TextFileAsynchronousLogWriter
      //
      /////////////////////////////////////////


      TextFileOutputStrategy::TextFileAsynchronousLogWriter::TextFileAsynchronousLogWriter() :
            stopped(false) {

         asynchWriteThreadPool = new ThreadPool(
            1,
            4096,
            false);

         TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread = new Threader();
         TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread->execute(this);

      }

      TextFileOutputStrategy::TextFileAsynchronousLogWriter::~TextFileAsynchronousLogWriter() {
         stop();
      }

      void TextFileOutputStrategy::TextFileAsynchronousLogWriter::setMaxWriteThreads(unsigned int max) {
         unsigned int maxWriteThreads = 1;
         if (max > 1) {
            maxWriteThreads = max;
         }
         asynchWriteThreadPool->setMaxThreads(maxWriteThreads);

      }

      void TextFileOutputStrategy::TextFileAsynchronousLogWriter::registerBuffer(
            const std::string & bufferName,
            std::shared_ptr<TextFileLogBuffer> buffer) {

         ScopedLock lock(bufferMapMutex);
         bufferMap.insert(std::make_pair(bufferName, buffer));
      }

      void TextFileOutputStrategy::TextFileAsynchronousLogWriter::removeBuffer(
            const std::string & bufferName) {

         ScopedLock lock(bufferMapMutex);
         std::map<std::string, std::shared_ptr<TextFileLogBuffer> >::iterator iter = bufferMap.find (bufferName);
         if (iter != bufferMap.end() ) {
            bufferMap.erase(iter);
         }
      }

      void TextFileOutputStrategy::TextFileAsynchronousLogWriter::execute(Threader *threader) {

         while (!stopped) {
            std::vector<std::shared_ptr<TextFileLogBuffer> > buffersThisPass;

            //if only using one logging thread, we're just going to write from this
            //asynchronous logging "control" thread and not use the pool
            bool usePool = asynchWriteThreadPool->getMaxThreads() > 1;

            try {
               {
                  ScopedLock lock(bufferMapMutex);
                  for ( std::map<std::string, std::shared_ptr<TextFileLogBuffer> >::const_iterator iter = bufferMap.begin();
                        iter != bufferMap.end();
                        iter++ ) {
                     if((*iter).second->snap()) {
                        buffersThisPass.push_back((*iter).second);
                     }
                  }
               }


               for ( std::vector<std::shared_ptr<TextFileLogBuffer> >::const_iterator iter = buffersThisPass.begin();
                     iter != buffersThisPass.end();
                     iter++ ) {
                  if (usePool) {
                     asynchWriteThreadPool->invoke((*iter).get());
                  }
                  else {
                     (*iter)->execute(threader);
                  }
               }

               if (usePool) {
                  asynchWriteThreadPool->wait();
               }

               //if there were no logs to be written this pass
               //  or if we aren't using the pool,
               //  or if we aren't using this asynchronous write thread at all (even though running)
               //we don't want this thread going into a tight loop
               //(note that the assumption here is that if we did write using the pool that
               // there was sufficient "interruption" deferring to the thread pool and rejoining
               // that a delay here is not needed to prevent this loop from running too tightly)
               if ((buffersThisPass.size() == 0) || (!usePool)){
                  threader->sleep(std::chrono::milliseconds(500));
               }

               buffersThisPass.clear();
            }
            catch (const Threader::Interrupted &) {
            }
            catch (const std::exception & e) {
               std::cerr << "TextFileAsynchronousLogWriter: Exception on write: " << e.what() << std::endl;
            }
            catch (...) {
               std::cerr << "TextFileAsynchronousLogWriter: Caught ... on write to file: " << std::endl;
            }
         }

         asynchWriteThreadPool->wait();

         // The thread's stop method has been called, make one more pass to make sure everything is
         // written.  (This probably is redundant, given that the log buffers flush to file on destruction)
         ScopedLock lock(bufferMapMutex);
         for ( std::map<std::string, std::shared_ptr<TextFileLogBuffer> >::const_iterator iter = bufferMap.begin();
               iter != bufferMap.end();
               iter++ ) {
            (*iter).second->execute(threader);
         }

         std::cout << "TextFileAsynchronousLogWriter: Exiting...." << std::endl;
      }


      void TextFileOutputStrategy::TextFileAsynchronousLogWriter::stop() {
         stopped = true;

         if (TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread != NULL) {
            std::cout << "TextFileAsynchronousLogWriter:logWriterThread running: "
                      << std::boolalpha << TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread->isRunning()
                      << std::endl;
            TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread->interrupt();
            TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread->join();
            delete TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread;
            TextFileOutputStrategy::TextFileAsynchronousLogWriter::logWriterThread = NULL;

            ScopedLock lock(bufferMapMutex);
            for ( std::map<std::string, std::shared_ptr<TextFileLogBuffer> >::const_iterator iter = bufferMap.begin();
                  iter != bufferMap.end();
                  iter++ ) {
               std::cout << "LEAKED BUFFER: " << iter->first << std::endl;
            }
         }
      }

}} //NAMESPACE

//------------------------------------------------------------------------------
//  UNCLASSIFIED (U)
//
//------------------------------------------------------------------------------
#ifndef _BLOCS_Threadable_H
#define _BLOCS_Threadable_H


namespace blocs {

      class Threader;

      /**
      Threadable class represents the active object in the thread.
      */

      class Threadable {

         public :

           virtual ~Threadable(){

           };

            virtual void execute(Threader *threader) = 0;

      };

} //NAMESPACE


#endif


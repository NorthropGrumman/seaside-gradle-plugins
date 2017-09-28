package com.ngc.seaside.service.helloworld;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Hello Tester.
 */
public class HelloTest {
   private Hello fixture;

   @Before
   public void before() throws Exception {
      fixture = new Hello();
   }

   @After
   public void after() throws Exception {
   }

   /**
    * Method: hello()
    */
   @Test
   public void testHello() throws Exception {
      assertEquals("Hello world!", fixture.sayHello());
   }

} 

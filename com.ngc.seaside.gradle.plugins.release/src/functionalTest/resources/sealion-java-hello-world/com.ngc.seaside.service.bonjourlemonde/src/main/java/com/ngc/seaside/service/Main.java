package com.ngc.seaside.service;

import com.ngc.seaside.service.bonjourlemonde.Bonjour;

public class Main {
   public static void main(String[] args) {
      Bonjour bonjour = new Bonjour();
      System.out.println(bonjour.disBonjour());
   }
}


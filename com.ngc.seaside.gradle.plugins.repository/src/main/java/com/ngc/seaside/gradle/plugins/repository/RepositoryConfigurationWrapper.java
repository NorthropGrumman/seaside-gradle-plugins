/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
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
package com.ngc.seaside.gradle.plugins.repository;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Wrapper class that ensures the extension has not already been configured when setting values and can set multiple
 * configurations with one method call.
 */
class RepositoryConfigurationWrapper implements RepositoryConfiguration {

   private final Runnable checkNotConfigured;
   private final RepositoryConfiguration[] configurations;

   RepositoryConfigurationWrapper(Runnable checkNotConfigured, RepositoryConfiguration... configurations) {
      if (checkNotConfigured == null || configurations == null || configurations.length == 0) {
         throw new IllegalArgumentException();
      }
      this.checkNotConfigured = checkNotConfigured;
      this.configurations = configurations;
   }

   @Override
   public String getName() {
      return getValue(RepositoryConfiguration::getName);
   }

   @Override
   public RepositoryConfiguration setName(String name) {
      return setValue(RepositoryConfiguration::setName, name);
   }

   @Override
   public String getUrlProperty() {
      return getValue(RepositoryConfiguration::getUrlProperty);
   }

   @Override
   public RepositoryConfiguration setUrlProperty(String urlProperty) {
      return setValue(RepositoryConfiguration::setUrlProperty, urlProperty);
   }

   @Override
   public String getUsernameProperty() {
      return getValue(RepositoryConfiguration::getUsernameProperty);
   }

   @Override
   public RepositoryConfiguration setUsernameProperty(String usernameProperty) {
      return setValue(RepositoryConfiguration::setUsernameProperty,
         usernameProperty);
   }

   @Override
   public String getPasswordProperty() {
      return getValue(RepositoryConfiguration::getPasswordProperty);
   }

   @Override
   public RepositoryConfiguration setPasswordProperty(String passwordProperty) {
      return setValue(RepositoryConfiguration::setPasswordProperty, passwordProperty);
   }

   @Override
   public boolean isRequired() {
      return getValue(RepositoryConfiguration::isRequired);
   }

   @Override
   public RepositoryConfiguration setRequired(boolean required) {
      return setValue(RepositoryConfiguration::setRequired, required);
   }

   @Override
   public boolean isAuthenticationRequired() {
      return getValue(RepositoryConfiguration::isAuthenticationRequired);
   }

   @Override
   public RepositoryConfiguration setAuthenticationRequired(boolean required) {
      return setValue(RepositoryConfiguration::setAuthenticationRequired, required);
   }

   private <T> T getValue(Function<RepositoryConfiguration, T> getter) {
      return getter.apply(configurations[0]);
   }

   private <T> RepositoryConfiguration setValue(BiFunction<RepositoryConfiguration, T, ?> setter,
            T value) {
      checkNotConfigured.run();
      for (RepositoryConfiguration config : configurations) {
         setter.apply(config, value);
      }
      return this;
   }
}

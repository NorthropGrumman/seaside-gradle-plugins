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
package com.ngc.seaside.gradle.plugins.repository;

/**
 * Default implementation of {@link RepositoryConfiguration}.
 */
public class DefaultRepositoryConfiguration implements RepositoryConfiguration {

   private String repositoryName;
   private String urlProperty;
   private String usernameProperty;
   private String passwordProperty;
   private boolean isRequired;
   private boolean isAuthenticationRequired;

   /**
    * Default constructor.
    */
   public DefaultRepositoryConfiguration() {}

   /**
    * Copy constructor.
    */
   public DefaultRepositoryConfiguration(RepositoryConfiguration config) {
      from(config);
   }

   @Override
   public String getName() {
      return repositoryName;
   }

   @Override
   public DefaultRepositoryConfiguration setName(String repositoryName) {
      this.repositoryName = repositoryName;
      return this;
   }

   @Override
   public String getUrlProperty() {
      return urlProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setUrlProperty(String urlProperty) {
      this.urlProperty = urlProperty;
      return this;
   }

   @Override
   public String getUsernameProperty() {
      return usernameProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setUsernameProperty(String usernameProperty) {
      this.usernameProperty = usernameProperty;
      return this;
   }

   @Override
   public String getPasswordProperty() {
      return passwordProperty;
   }

   @Override
   public DefaultRepositoryConfiguration setPasswordProperty(String passwordProperty) {
      this.passwordProperty = passwordProperty;
      return this;
   }

   @Override
   public boolean isRequired() {
      return isRequired;
   }

   @Override
   public DefaultRepositoryConfiguration setRequired(boolean required) {
      this.isRequired = required;
      return this;
   }

   @Override
   public DefaultRepositoryConfiguration optional() {
      setRequired(false);
      return this;
   }

   @Override
   public boolean isAuthenticationRequired() {
      return isAuthenticationRequired;
   }

   @Override
   public DefaultRepositoryConfiguration setAuthenticationRequired(boolean required) {
      this.isAuthenticationRequired = required;
      return this;
   }

}

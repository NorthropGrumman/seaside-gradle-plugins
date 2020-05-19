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
 * Property details for a repository.
 */
public interface RepositoryConfiguration {

   /**
    * Returns the descriptive name of the repository.
    * 
    * @return the descriptive name of the repository.
    */
   String getName();

   /**
    * Sets the descriptive name of the repository.
    * 
    * @param repositoryName the descriptive name of the repository
    * @return this
    */
   RepositoryConfiguration setName(String repositoryName);

   /**
    * Returns the property key for determining the url of the repository.
    * 
    * @return the property key for determining the url of the repository
    */
   String getUrlProperty();

   /**
    * Sets the property key for determining the url of the repository.
    * 
    * @param urlProperty the property key for determining the url of the repository
    * @return this
    */
   RepositoryConfiguration setUrlProperty(String urlProperty);

   /**
    * Returns the property key for determining the username of the repository authentication.
    * 
    * @return the property key for determining the username of the repository authentication
    */
   String getUsernameProperty();

   /**
    * Sets the property key for determining the username of the repository authentication.
    * 
    * @param usernameProperty the property key for determining the username of the repository authentication
    * @return this
    */
   RepositoryConfiguration setUsernameProperty(String usernameProperty);

   /**
    * Returns the property key for determining the password of the repository authentication.
    * 
    * @return the property key for determining the password of the repository authentication
    */
   String getPasswordProperty();

   /**
    * Sets the property key for determining the password of the repository authentication.
    * 
    * @param passwordProperty the property key for determining the password of the repository authentication
    * @return this
    */
   RepositoryConfiguration setPasswordProperty(String passwordProperty);

   /**
    * Returns whether or not the repository's property is required to be set.
    * 
    * @return whether or not the repository's property is required to be set
    */
   boolean isRequired();

   /**
    * Sets whether or not the repository's property is required to be set.
    * 
    * @param required whether or not the repository's property is required to be set
    * @return this
    */
   RepositoryConfiguration setRequired(boolean required);

   /**
    * Requires that the repository's property be set.
    * 
    * @return this
    */
   default RepositoryConfiguration required() {
      return setRequired(true);
   }

   /**
    * Sets the requirement that the repository's property be set to optional.
    * 
    * @return this
    */
   default RepositoryConfiguration optional() {
      return setRequired(false);
   }

   /**
    * Returns whether or not the repository's username and password properties are required to be set.
    * 
    * @return whether or not the repository's username and password properties are required to be set
    */
   boolean isAuthenticationRequired();

   /**
    * Sets whether or not the repository's username and password properties are required to be set.
    * 
    * @param required whether or not the repository's username and password properties are required to be set
    * @return this
    */
   RepositoryConfiguration setAuthenticationRequired(boolean required);

   /**
    * Requires that the repository's username and password properties be set.
    * 
    * @return this
    */
   default RepositoryConfiguration authenticationRequired() {
      return this.setAuthenticationRequired(true);
   }

   /**
    * Sets the requirement that the repository's username and password properties be set to optional.
    * 
    * @return this
    */
   default RepositoryConfiguration authenticationOptional() {
      return this.setAuthenticationRequired(false);
   }

   /**
    * Copies the given repository configuration to this configuration.
    * 
    * @param configuration other configuration
    * @return this
    */
   default RepositoryConfiguration from(RepositoryConfiguration configuration) {
      this.setName(configuration == null ? null : configuration.getName());
      this.setUrlProperty(configuration == null ? null : configuration.getUrlProperty());
      this.setUsernameProperty(configuration == null ? null : configuration.getUsernameProperty());
      this.setPasswordProperty(configuration == null ? null : configuration.getPasswordProperty());
      this.setAuthenticationRequired(configuration == null ? null : configuration.isAuthenticationRequired());
      this.setRequired(configuration == null ? null : configuration.isRequired());
      return this;
   }

}

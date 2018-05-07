package com.ngc.seaside.gradle.plugins.repository;

/**
 * Property details for a repository.
 */
public class RepositoryConfiguration {

   private String repositoryName;
   private String repositoryProperty;
   private String usernameProperty;
   private String passwordProperty;
   private boolean isRequired;
   private boolean isAuthenticationRequired;

   /**
    * Returns the descriptive name of the repository.
    * 
    * @return the descriptive name of the repository.
    */
   public String getName() {
      return repositoryName;
   }

   /**
    * Sets the descriptive name of the repository.
    * 
    * @param repositoryName the descriptive name of the repository
    * @return this
    */
   public RepositoryConfiguration setName(String repositoryName) {
      this.repositoryName = repositoryName;
      return this;
   }

   /**
    * Returns the property key for determining the url of the repository.
    * 
    * @return the property key for determining the url of the repository
    */
   public String getUrlProperty() {
      return repositoryProperty;
   }

   /**
    * Sets the property key for determining the url of the repository.
    * 
    * @param repositoryProperty the property key for determining the url of the repository
    * @return this
    */
   public RepositoryConfiguration setUrlProperty(String repositoryProperty) {
      this.repositoryProperty = repositoryProperty;
      return this;
   }

   /**
    * Returns the property key for determining the username of the repository authentication.
    * 
    * @return the property key for determining the username of the repository authentication
    */
   public String getUsernameProperty() {
      return usernameProperty;
   }

   /**
    * Sets the property key for determining the username of the repository authentication.
    * 
    * @param usernameProperty the property key for determining the username of the repository authentication
    * @return this
    */
   public RepositoryConfiguration setUsernameProperty(String usernameProperty) {
      this.usernameProperty = usernameProperty;
      return this;
   }

   /**
    * Returns the property key for determining the password of the repository authentication.
    * 
    * @return the property key for determining the password of the repository authentication
    */
   public String getPasswordProperty() {
      return passwordProperty;
   }

   /**
    * Sets the property key for determining the password of the repository authentication.
    * 
    * @param passwordProperty the property key for determining the password of the repository authentication
    * @return this
    */
   public RepositoryConfiguration setPasswordProperty(String passwordProperty) {
      this.passwordProperty = passwordProperty;
      return this;
   }

   /**
    * Returns whether or not the repository's property is required to be set.
    * 
    * @return whether or not the repository's property is required to be set
    */
   public boolean isRequired() {
      return isRequired;
   }

   /**
    * Sets whether or not the repository's property is required to be set.
    * 
    * @param required whether or not the repository's property is required to be set
    * @return this
    */
   public RepositoryConfiguration setRequired(boolean required) {
      this.isRequired = required;
      return this;
   }

   /**
    * Requires that the repository's property be set.
    * 
    * @return this
    */
   public RepositoryConfiguration required() {
      setRequired(true);
      return this;
   }

   /**
    * Sets the requirement that the repository's property be set to optional.
    * 
    * @return this
    */
   public RepositoryConfiguration optional() {
      setRequired(false);
      return this;
   }

   /**
    * Returns whether or not the repository's username and password properties are required to be set.
    * 
    * @return whether or not the repository's username and password properties are required to be set
    */
   public boolean isAuthenticationRequired() {
      return isAuthenticationRequired;
   }

   /**
    * Sets whether or not the repository's username and password properties are required to be set.
    * 
    * @param required whether or not the repository's username and password properties are required to be set
    * @return this
    */
   public RepositoryConfiguration setAuthenticationRequired(boolean required) {
      this.isAuthenticationRequired = required;
      return this;
   }

   /**
    * Requires that the repository's username and password properties be set.
    * 
    * @return this
    */
   public RepositoryConfiguration authenticationRequired() {
      setAuthenticationRequired(true);
      return this;
   }

   /**
    * Sets the requirement that the repository's username and password properties be set to optional.
    * 
    * @return this
    */
   public RepositoryConfiguration authenticationOptional() {
      setAuthenticationRequired(false);
      return this;
   }
}

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

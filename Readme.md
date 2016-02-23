# Apprenda Support for Bamboo

## Introduction
This plugin provides tasks to deploy artifacts to the Apprenda Platform.
It supports both build plans and deployment projects.

## Installation
1. Either build the add-on from source or download the plugin using the link provided by client services.
2. After logging into the Bamboo, navigate to the downloaded add-on .obr or .jar file and click upload.
3. Once successful you'll be presented with information about the add-on.
4. To confirm the modules are running you can click on the link "2 of 2 modules enabled".

## Building from source
### Prerequisites
* You'll need the Java JDK 1.6 or higher.

1. Install the [Atlassian SDK](https://developer.atlassian.com/docs/getting-started)
2. Clone the source 
`git clone git@bitbucket.org:apprendaclientservices/apprenda-bamboo.git`
3. Navigate to the working directory of the apprenda-bamboo project.
4. Execute `atlas-mvn package`
5. The plugin will be in the target subdirectory target and be called apprenda-bamboo-<version number>.obr
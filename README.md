# Spring-Boot-Starter-Ocep

## Getting started

This is an OCEP base project which contains the essential dependencies and configurations for creating other OCEP applications. Next time you need to create a new OCEP project, simply just fork this project and rename it appropriately.

***

##Folder Structure Conventions

A typical top-level directory OCEP layout:

    .
    ├── jenkins                 # Contains jenkins deployment files
    ├── aspects                 # AOP files 
    ├── configurations          # Default configurations/properties' files 
    ├── controllers             # Rest Controller files
    ├── enums                   # Enumeration files
    ├── models                  # Entity and data files
    ├── services                # Service files
    ├── util                    # Tools and utilities files
    └── resources ├── templates        # Fragments, OCEP pages and XSDs
                  ├── application.yml  # Local, Linux, Windows and Cloud properties
    ├── build-config.json       # Openshift build configurations
    ├── deployment-config.json  # Openshift deployment configuration
    ├── image-stream.json       # Openshift image stream

Please rename **"spring-boot-starter-ocep"** to your new OCEP project name in all relevant files.

## Installation

When you're ready to create a new OCEP project, simply fork/clone this repository and use it as a handy template.

**How To Fork The Repository**

1. In the upper-right corner, select Fork. 
2. Choose a namespace for your fork.
3. The project becomes available at https://gitlab.com/<your-namespace>/sample-project/.

**How To Clone The Repository**

1. Authenticate with GitLab by following the instructions in their SSH documentation.
2. Go to your project’s landing page and select Clone. Copy the URL for Clone with SSH.
3. Open a terminal and go to the directory where you want to clone the files. Git automatically creates a folder with the repository name and downloads the files there.
4. Run this command, then cd spring-boot-starter-ocep:

    ```
    git clone https://rmb-rbvprtool01:7990/scm/rocep/spring-boot-starter-ocep.git
    ```

5. If you run into the following error: "fatal: unable to access 'https://rmb-rbvprtool01:7990/scm/rocep/spring-boot-starter-ocep.git/': SSL certificate problem: self-signed certificate in certificate chain"
6. Run this command 

    ```
    git config --global http.sslVerify false
    ```

7. Rerun the git clone command

## How to use this project
Once you decided how you want to get the project(fork or clone), please rename **"spring-boot-starter-ocep"** to your new OCEP project name. Then make sure all files containing the name **"spring-boot-starter-ocep"** are all renamed to your project name.

##Files to rename

|               File Name               |                                           File Location                                            |                             Line Number to Rename                              |                                                                                               Content to Rename                                                                                               |
|:-------------------------------------:|:--------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|          Jenkinsfile.deploy           |                                     jenkins/Jenkinsfile.deploy                                     |                                    Line #3                                     |                                                                                 def templateName = 'spring-boot-starter-ocep'                                                                                 |
|        Jenkinsfile.multibranch        |                                  jenkins/Jenkinsfile.multibranch                                   |                       Line #1<br/>Line #3<br/>Line #7&8                        |    def sonarProjectKey = 'springBootStarterOcep' <br/>def deploymentProjectName = 'Spring_Boot_Starter_Ocep_Deployment' <br/>  def OPEN_SHIFT_NAMESPACE = 'rmb-ocep-core-dev' and remove comment in line 7    |
|          Jenkinsfile.promote          |                                    jenkins/Jenkinsfile.promote                                     |                                    Line #1                                     |                                                                                 def templateName = 'spring-boot-starter-ocep'                                                                                 |
|  ApplicationConfigurationProperties   | src/main/java/rmb/ocep/springbootstarterocep/configuration/ApplicationConfigurationProperties.java |                             Line #19<br/>Line #23                              |                                                             private String springBootStarterOcepHost; <br/> + "spring-boot-starter-ocep: [{}]\n",                                                             |
|         application-local.yml         |                              src/main/resources/application-local.yml                              |                                    Line #5                                     |                                                                                           spring-boot-starter-ocep:                                                                                           |
|  application-local-linux-docker.yml   |                       src/main/resources/application-local-linux-docker.yml                        |                                    Line #5                                     |                                                                                           spring-boot-starter-ocep:                                                                                           |
| application-local-windows-docker.yml  |                      src/main/resources/application-local-windows-docker.yml                       |                                    Line #5                                     |                                                                                           spring-boot-starter-ocep:                                                                                           |
|             bootstrap.yml             |                                           bootstrap.yml                                            |                                    Line #3                                     |                                                                                        name: spring-boot-starter-ocep                                                                                         |
|           build-config.json           |                                         build-config.json                                          |                  Line #6<br/>Line #7<br/>Line #9<br/>Line #17                  |                                               "build": "spring-boot-starter-ocep"<br/>"app": "spring-boot-starter-ocep"<br/>"name": "spring-boot-starter-ocep"                                                |
|        deployment-config.json         |                                       deployment-config.json                                       | Line #5, #13, #15, #21, #22, #30, #31, #38, #105, #109, #125, #127, #139, #140 |                                                                                    Replace all "spring-boot-starter-ocep"                                                                                     |
|           image-stream.json           |                                         image-stream.json                                          |                        Line #6<br/>Line #7<br/>Line #9                         |                                               "build": "spring-boot-starter-ocep"<br/>"app": "spring-boot-starter-ocep"<br/>"name": "spring-boot-starter-ocep"                                                |
|                pom.xml                |                                              pom.xml                                               |                       Line #13<br/>Line #15<br/>Line #16                       | <artifactId>spring-boot-starter-ocep</artifactId><br/><description>Contains essential dependencies and configurations for creating other ocep projects</description><br/><name>SpringBoot Starter Ocep</name> |
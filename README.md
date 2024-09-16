# Spring-Boot-Starter-TTS

## Getting started

This is an TTS base project which contains the essential dependencies and configurations for creating other TTS applications. Next time you need to create a new TTS project, simply just fork this project and rename it appropriately.

***

## Folder Structure Conventions

A typical top-level directory TTS layout:

    .
    ├── jenkins                 # Contains jenkins deployment files
    ├── aspects                 # AOP files 
    ├── configurations          # Default configurations/properties' files 
    ├── controllers             # Rest Controller files
    ├── enums                   # Enumeration files
    ├── models                  # Entity and data files
    ├── services                # Service files
    ├── util                    # Tools and utilities files
    └── resources ├── templates        # Fragments, TTS pages and XSDs
                  ├── application.yml  # Local, Linux, Windows and Cloud properties
    ├── build-config.json       # Openshift build configurations
    ├── deployment-config.json  # Openshift deployment configuration
    ├── image-stream.json       # Openshift image stream

Please rename **"spring-boot-starter-tts"** to your new TTS project name in all relevant files.

## Installation

When you're ready to create a new TTS project, simply fork/clone this repository and use it as a handy template.

**How To Fork The Repository**

1. In the upper-right corner, select Fork. 
2. Choose a namespace for your fork.
   3. The project becomes available at https://[SERVER]:[PORT]/projects/[NAMESPACE]/repos/[REPO-NAME]/

**How To Clone The Repository**

1. Authenticate with BitBucket by following the instructions in their SSH documentation.
2. Go to your project’s landing page and select Clone. Copy the URL for Clone with SSH (For help setting up SSH, visit https://wiki.rmb.co.za:8443/display/ROC/Cloning+from+BitBucket%3A+A+quick+guide+to+the+galaxy+of+our+projects.
3. Open a terminal and go to the directory where you want to clone the files. Git automatically creates a folder with the repository name and downloads the files there.
4. Run this command, then cd spring-boot-starter-tts:

    ```
    git clone ssh://git@rmb-rbvprtool01:7999/rocep/tts-spring-boot-starter-ocep.git
    ```

## How to use this project
Once you decided how you want to get the project(fork or clone)

1. Determine whether OAuth2 is necessary for your application. If it is, update the configuration in SecurityConfig class under configuration. Otherwise, remove the comment in the SecurityConfig class
2. Rename **"spring-boot-starter-tts"** to your new TTS project name. Then make sure all files containing the name **"spring-boot-starter-tts"** are all renamed to your project name.

##Files to rename

|              File Name               |                                          File Location                                           |                             Line Number to Rename                              |                                                                                             Content to Rename                                                                                              |
|:------------------------------------:|:------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|          Jenkinsfile.deploy          |                                    jenkins/Jenkinsfile.deploy                                    |                                    Line #3                                     |                                                                                def templateName = 'spring-boot-starter-tts'                                                                                |
|       Jenkinsfile.multibranch        |                                 jenkins/Jenkinsfile.multibranch                                  |                       Line #1<br/>Line #3<br/>Line #7&8                        |    def sonarProjectKey = 'springBootStarterTTS' <br/>def deploymentProjectName = 'Spring_Boot_Starter_TTS_Deployment' <br/>  def OPEN_SHIFT_NAMESPACE = 'rmb-tts-core-dev' and remove comment in line 7    |
|         Jenkinsfile.promote          |                                   jenkins/Jenkinsfile.promote                                    |                                    Line #1                                     |                                                                                def templateName = 'spring-boot-starter-tts'                                                                                |
|  ApplicationConfigurationProperties  | src/main/java/rmb/tts/springbootstartertts/configuration/ApplicationConfigurationProperties.java |                             Line #19<br/>Line #23                              |                                                            private String springBootStarterTTSHost; <br/> + "spring-boot-starter-tts: [{}]\n",                                                             |
|        application-local.yml         |                             src/main/resources/application-local.yml                             |                                    Line #5                                     |                                                                                          spring-boot-starter-tts:                                                                                          |
|  application-local-linux-docker.yml  |                      src/main/resources/application-local-linux-docker.yml                       |                                    Line #5                                     |                                                                                          spring-boot-starter-tts:                                                                                          |
| application-local-windows-docker.yml |                     src/main/resources/application-local-windows-docker.yml                      |                                    Line #5                                     |                                                                                          spring-boot-starter-tts:                                                                                          |
|            bootstrap.yml             |                                          bootstrap.yml                                           |                                    Line #3                                     |                                                                                       name: spring-boot-starter-tts                                                                                        |
|          build-config.json           |                                        build-config.json                                         |                  Line #6<br/>Line #7<br/>Line #9<br/>Line #17                  |                                               "build": "spring-boot-starter-tts"<br/>"app": "spring-boot-starter-tts"<br/>"name": "spring-boot-starter-tts"                                                |
|        deployment-config.json        |                                      deployment-config.json                                      | Line #5, #13, #15, #21, #22, #30, #31, #38, #105, #109, #125, #127, #139, #140 |                                                                                   Replace all "spring-boot-starter-tts"                                                                                    |
|          image-stream.json           |                                        image-stream.json                                         |                        Line #6<br/>Line #7<br/>Line #9                         |                                               "build": "spring-boot-starter-tts"<br/>"app": "spring-boot-starter-tts"<br/>"name": "spring-boot-starter-tts"                                                |
|               pom.xml                |                                             pom.xml                                              |                       Line #13<br/>Line #15<br/>Line #16                       | <artifactId>spring-boot-starter-tts</artifactId><br/><description>Contains essential dependencies and configurations for creating other tts projects</description><br/><name>SpringBoot Starter tts</name> |

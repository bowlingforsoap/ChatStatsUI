# STATISTICS VISUALISATION APPLICATION
====================================
Requirements:
----/-------
- Java 8
- Gradle 2.6

Deployment:
----/-----
Jenkins: QuickBlox-Chat-Stats-Application

Run application production mode (manual):
-------------/--------------------------
1. Specify *chat_home* property (path to Quickblox-Tigase chat) in **conf/application.conf** (this is required by the application to get acces to properties in **etc/init.properties**)
2. Navigate to project's root (where **build.gradle** file is located)
3. Run: 'gradle dist'
4. Get the archive at ./build/distributions/playBinary.zip
5. Unzip and navigate to root (aka place, where **bin**, **conf** and **lib** folders are located)
6. Run bash chat-stats-ui start ($JAVA_HOME must point to Java 8 installation!)

Run application dev mode:
-----------/------------
1. Navigate to project's root (where **build.gradle** file is located)
2. Run: 'gradle run'
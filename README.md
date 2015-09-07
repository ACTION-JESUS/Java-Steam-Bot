ACTION JESUS' setup notes
=========================

Steamkit project setup

1) Create a jar file from the classes in "resources":
	cd SteamKit-Java/src/main/resources/uk/co/thomasc/steamkit/base/generated
	jar cvf steamkit_resources.jar *.class
	add the new steamkit_resources.jar file to Java Build Path->Libraries->Add External JARs

2) Download and add the following JAR files to to Java Build Path->Libraries->Add External JARs:
	bcprov-ext-jdk14-1.47.jar
	hamcrest-core-1.3.jar
	junit-4.10.jar
	log4j-1.2.17.jar
	lombok.jar
	proto.jar
	protobuf-java-2.4.1.jar (one of these proto files is not used)

3) Lombok setup:
	Download the jar from : https://projectlombok.org/
	Execute "java -jar lombok.jar" to install it in Eclipse
	Place lombok.jar in the root project folder

4) Install Java encryption files
	Download jce_policy-8.zip and unzip to $JAVA_HOME/jre/lib/security 


Steambot project setup

1) Download and add the following JAR files to to Java Build Path->Libraries->Add External JARs:
	hamcrest-core-1.3.jar
	junit-4.10.jar

2) Update the mvmlobbybot.properties file with your bot's parameters

3) Option: Update the SteamBot.onChatInviteCallback() code with your own lobby message

4) Create a run configuration using com.mvmlobby.main.MvMLobbyBot as the "Main class"

5) To create a JAR file that your bot can run anywhere:
	a) right click on the project
	b) select "Export"
	c) select Java -> Runnable JAR file
	d) Launch Configuration = the run configuration above
	e) Select any Export destination file
	f) Select "Package required libraries into generated JAR"

6) Copy and execute the JAR anywhere with:
	java -jar lobbybot.jar

7) If you run into a "unsigned jar" type error, sign it with these steps:
	a) create a key with something like "keytool -genkey -alias botjarkeys -keystore ~/dev/jarsigning/.keystore"
	b) sign the jar with "jarsigner -keystore ~/dev/jarsigning/.keystore lobbyidbot.jar botjarkeys"


# Java-Steam-Bot
Most of the code comes from a fork from here:  https://github.com/jhomlala/Java-Steam-Bot
Thanks to jhomlala for getting this working.
```




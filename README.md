# CodeNamesPlayingAgent
In this repository is shared the SDAI and NLP project of student Samuele Capani from Università degli studi di Genova

# Instructions to run the application
Download the jars from:
https://drive.google.com/drive/folders/1x0vLjGxTinh0-6UynAHNtMdIEQkr4BpL?usp=drive_link

First of all start the webserver with webServer.jar. After the webserver is online start the application with agents.jar.

It's possible to customize the names, the number and the selected strategy of the agents appending $--$conf FILEPATH to the command, where FILEPATH points to a file with the same shape of ExampleConfig.json that can be found into the project root.


# Bootstrap for developers/ using an Ide
Steps to do in order to run WebServer:
	- run the main inside 'WebServerApplication.java'

Steps to do in order to run Agents:
	- download jade from https://jade.tilab.com/
		- click on 'Download' tab
		- click on 'Jade'
		- click on continue and read the terms
		- click on 'yes, I agree'
		- click on 'jadeBin' to finally download a .zip
		- extract the zip and annotate the path of the jade.jar
	- if using Eclipse: in the .classpath file write the path of the jade.jar (jade.jar included) inside the pointed element and in the pom.xml delete the jade dependency
	- if using Visual Studio Code: 
		- in a terminal opened inside .\Agents
		- execute this command: mvn install:install-file  -Dfile="<JADEPATH>/jade.jar"  -DgroupId="com.tilab.jade"  -DartifactId="jade"  -Dversion="4.5.0"  -Dpackaging="jar"
	- download the embeddings at https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit?usp=sharing
		- this file is not in the repository, but it's mandatory, alternatively different embeddings can be used, if compatible with the Word2Vec library
	- move the extracted file into this folder 'Agents/src/main/resources'
	- build the project
	- now you can run the main inside exec/RunJade.java (after running the webServer)
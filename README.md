# FX_Chat
This is the multiuser secure chat on JavaFX platform.
Steps to run it:
1. Clone/or download repository using link https://github.com/shpashkeev/FX_Chat.git 
2. Install java (8 version or less) link: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
3. Download maven https://maven.apache.org/download.cgi
4. Unzip downloaded maven and add path of maven/bin to PATH environment variable
5. Run cmd and select the folder with the project (\\FX_Chat\server)
6. Enter in cmd: mvn compile 
7. Enter in cmd: mvn exec:java -Dexec.mainClass="app.netty.server.SecureChatServer" -Dexec.args="8007"
(if build faild, try again and then build will be sucessful), (-Dexec.args="8007" - desired launch port)
8. Open as cmd number as many users you want 
9. Go in cmds to the folder with project(\\FX_Chat\client)and enter in each cmd such script:
mvn compile
mvn exec:java -Dexec.mainClass="application.SecureChatClient"
(you can try to run client project on different devices, remember that number of running client project = number of users)
10. Chatting

OOP Example: https://github.com/shpashkeev/FX_Chat/blob/master/client/src/application/SecureChatClientHandler.java  (all file)
or https://github.com/shpashkeev/FX_Chat/blob/master/client/src/application/SecureChatClient.java 28 line.

Pattern: MVC 
Model - Client (https://github.com/shpashkeev/FX_Chat/blob/master/client/src/application/SecureChatClient.java)
View - FXML file (https://github.com/shpashkeev/FX_Chat/blob/master/client/src/ChatClient.fxml)
Controller - class Controller for fxml file (https://github.com/shpashkeev/FX_Chat/blob/master/client/src/application/SecureChatClientController.java)


# Renetty Project
the goal of this project is to simplify the usage of Netty. 
currently, it provides a set of annotations to handle http rest calls easily.
 
 ## How to Build
 
 ### Manual Build
 Requirements:
 - Java 1.7+
 - Apache Maven
 ```
 $ mvn clean install
```

finally, add the following dependency to the project's pom.xml (in case of using maven)
```
<dependency>
    <groupId>com.github.nimabt.renetty</groupId>
    <artifactId>renetty-http</artifactId>
    <version>0.1.3</version>
</dependency>
```
### Using JitPack
you can also use [jitpack](https://jitpack.io) to prevent local build & automatically integrate the required dependency into your project
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

...
    
<dependencies>
    ...
    <dependency>
        <groupId>com.github.nimabt.renetty</groupId>
        <artifactId>renetty-http</artifactId>
        <version>0.1.3</version>
    </dependency>
    ...
</dependencies>

    

```
 
 ## Sample Usage
 
 **TestHttpServerLauncher.java**
 ```
 final TestHttpHandler handler = new TestHttpHandler();
 final int port = 8090;
 final int workerCount = 1000;
 final int maxContentLength = 65535;
 final NettyHttpServer nettyHttpServer = new NettyHttpServer(port,handler,workerCount,maxContentLength);
 nettyHttpServer.start();
 ```
 
 **TestHttpHandler.java**
 ```
 ...
 
 @HttpRequest(method = RequestMethod.GET, path = "/test")
 public String testGet(){
     return "test httpResponse";
 }
 @HttpRequest(method = RequestMethod.POST , path="/test/post/data")
 public String testPostData(final @RequestBody String body, final @IpAddress String ipAddress){
     return "post got value: " + body + " ,from: " + ipAddress;
 }   
 ...

 ```
 
please check the package:example for more samples.
 
 
 ## final note
 it's just the beginning of the development process, so feel free to send me a message if there's anything in your mind. 
 
 
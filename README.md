# [20CS038-webapp](https://github.com/kinhochu2/20CS038-webapp) & 20CS038-server

Installation Guide
===========
Requirements:
-----
-	A web3 provider (e.g.: Geth, ganache, Infura)
-	Eclipse with Java 8+
-	Web3j CLI (If you want to test the Solidity contract)
-	Android device or a simulator

QuickStart
-------
The simplest way to start is to download the source code to your device. The project contains three parts: Ionic web app, Java server and Solidity contract. The contracts are already wrapped and imported to the Java server so there is no need to convert it again.
For the web app, you can just download the .apk file to your android device or simulator to run. Please note that the recommended version for the Android device should be at least 8.0 or higher (Android 8.0 Oreo), this is to ensure that there will be no compatibility issue occurred in the app. 
For the java server, since it is a maven-type project, it cannot be converted into a .jar file so there is no workaround. Please download the full source code to your Eclipse IDE. Once you have download, pressing “Run” and “Build” should be able to start a HTTP server on the localhost.

Build Instruction
------
If you want to check out the source code of the app, you can download the code to your code editor (e.g.: Visual Studio Code) and type the following commands:
npm install
```
ionic serve
```
These two commands will install the dependencies and run a web version of the app on your default browser. It still contains most of the functions of the app. If you want to test it on your Android device, please type the following command:
```
ionic platform add android
ionic cordova prepare android
```
These two commands will add a converted version of the android app to the project folder. You will need to open it in an Android Java editor (e.g.: Android Studio) and build it. The path to the app should be “EthshShip/platform/android”.
For the smart contracts, the .sol files are already provided in the software folder. As said before there is no need to download it if you just want to run the app. But if you want to test it on the java server, please type the following commands:
```
web3j generate solidity -a <contract name>.abi -b <contract name>.bin -o <output path> -p <package name>
```

Note
-------
### Web3 Provider ###

The default web3 provider in the project is Ganache (HTTP://127.0.0.1:7545). If you want to change it to the other providers. Please go to line 70 and 72 of Web3Provider.java located in server: “EthsyShip\src\main\java\cityu.cs.fyp.java_ethereum” and change the URL of the selected provider.
 ```
 if(this.webj == null)
    this.web3j = Web3j.build(new HttpService(<web3 provider>));
 if(this.admin == null)
    this.admin = Admin.build(new HttpService(<web3 provider>));
 ```
### Firebase ###
To access the Firebase service, you would need to register an app on Firebase console and retrieve the serviceAccount.json for verification. Currently the file is stored in local computer and could not be used once switched to other devices. Moreover, the access to the Firestore database has a limited usage of around 15,000 transaction per month so please be aware. Once you have retrieved the serviceAccount.json, please go to line 21 of FirebaseAppInitializer.java located in server: “EthsyShip\src\main\java\cityu.cs.fyp.firebase” and change the path to the new JSON file.
 ```
 serviceAccount = new FileOutputStream(<service account path>);
 credentials = GoogleCredentials.fromStream(serviceAccount);
 ```
### MapQuest ###
The app currently uses MapQuest to generate static maps and markers, with a free and limited account. The key provided has a limited usage of 1,500 transaction per month, which includes geocoding, map loading, directions, etc. It is very likely that at the time of compiling the app, the usage is already full. In that case, please register a new account and use the new key instead. Once you have registered a key, please go to line 14 of LocationTrackingService.ts located in web app: “EthsyShip\src\services\Location-tracking” and change the key to the new key.
 ```
 const MapQuestKey = <MapQuest key>
 ```

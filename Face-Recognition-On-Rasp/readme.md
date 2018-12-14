# Raspberry PI - Face Recognition App
This is project about system sercurity in the building use raspberry pi with MFRC522 and camera.
## Feature:
1. Devide level of permission of user.
2. Set, change permission of the door.
2. Sercurity in door of sensitive position (sercurity gate, entrance door, safe, latibute, hight floor).
3. Look all the door to protect when found thrieves.
4. Recognition user and send to the server.
5. Log

## Decription:
1. gateway(raspberry pi 3): manage the door(open, close). Write by AndroidThing
2. server(localhost). Nodejs, socket-io,
3. WebApp for admin

## Requirement:
* hardware: MFRC522, card, raspberry pi (3B), server(localhost).
* software:
- Mongod start service in linux as ```sudo systemctr start mongo```
- Port 3000, 4000 in localhost is unused. check with ```netstat```
- python 3
- OpenCv (>= 3.4.2)
- package on python: 
  + imutils
  + dlib
  + numpy
  + scikit-learn
- nodejs, npm

## Installation (build from source)
Run Server
1. Pull the latest commit of master branch (or another)
```
git clone https://github.com/ad0min/Android-Thing-Raspberry-Pi-3B.git
```
2. Navigator to folder source Face Recognition App ```cd Face-Recognition-On-Rasp```
2. Direct to Server folder: ```cd server```
2. Create new file `.env` by copy file `.env-example`
3. Make sure that your computer have installed Nodejs, ...
4. Install the dependent packages: ```npm install```
5. Run server: ```npm start```
6. To test server, go to url http://localhost:3000.
7. Login webapp with account admin 
```
username = 'admin'
password = 'admin'
``` 

### Connect rasp pi to Server
Build and Run source from folde './android' in raspberry 
```Change Ip, Port by Ip, Port of server in file Config```

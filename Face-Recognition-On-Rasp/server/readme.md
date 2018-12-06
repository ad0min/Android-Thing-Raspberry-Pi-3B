# Raspberry PI Server

## To start database, do the following steps:
- Make sure that your computer have installed MongoDb.
- Make sure you can run command ```mongod``` from Terminal (or CMD)
- Direct to server/database folder:```cd server/database```
- Run database server: ```mongod --config mongo.config```
- If the output showed errors, you must open mongo.config file and change to correct path

## To start server, do the following steps:
- Pull the latest commit of master branch (or another)
- Direct to Server folder: ```cd server```
- Make sure that your computer have installed Nodejs
- Install the dependent packages: ```npm install```
- Run server: ```npm start```
- To test server, go to url localhost:80.
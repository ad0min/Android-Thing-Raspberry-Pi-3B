require('dotenv').config();
const mongoose = require('mongoose');
const { Schema, SchemaType } = mongoose;
require('./mongooseConnection');
const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const _ = require('lodash');
var fs = require('fs');

const faceRegconitionHeper = require('./face_recognition/faceRegconitionHeper');

const modelType = require('./models/type');
const doorModel = mongoose.model(modelType.doorType);
const userModel = mongoose.model(modelType.userType);
const logModel = mongoose.model(modelType.logType);
const departmentModel = mongoose.model(modelType.departmentType);
const permissionModel = mongoose.model(modelType.permissionType);


const { CODE_AUTHEN, CODE_REQUEST_CLOSE_DOOR, CODE_ERROR, CODE_REQUEST_LOCK_DOOR, CODE_REQUEST_OPEN_DOOR, CODE_RASP_INFO, CODE_CHECK_PERMISION_CARD, CODE_REQUEST_RASP_INFO, CODE_REQUEST_TAKE_PICTURE, CODE_REQUEST_WRITE_CARD, CODE_SUCCESS, MESSAGE_SUCCESS} = require('./const');

const socketList = {};

app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});

io.on('connection', socket => {
    console.log('Raps connected');
    socketList[socket.id] = {
        authen: false,
        socket: socket,
    };
    socket.on('event', (msg, callback) => {
        console.log(msg);
        const [code, data, error, ...opts] = msg.split(';');
        console.log(socketList);
        if (!socketList[socket.id].authen) {
            if(code === CODE_AUTHEN){
                const res = process.env.SERVER_KEY_AUTHEN === data;
                socketList[socket.id].authen = res;
                callback(res? CODE_SUCCESS : CODE_ERROR + `;${MESSAGE_SUCCESS}`);
                if (!res) {
                    socket.disconnect(true);
                }
            }
            else {
                console.log(`Rasp ${socket.id} not authen`);
                callback(CODE_ERROR + `;You not authen`);
            }
        }
        else{
            const responseCode = {
                '002': async (socketList, socket, code, data) => {
                    try {
                        const tmp = await doorModel.findById(data).exec();
                        if (tmp){
                            _.set(socketList, socket.id.doorId, data);
                            callback(CODE_SUCCESS +`;${MESSAGE_SUCCESS}`);
                        }
                        else {
                            callback(CODE_ERROR +`;Wrong data`);
                        }
                    }catch (error){
                        callback(CODE_ERROR + `;Database error`);
                    }
                },
                '300': async(socketList, socket, code, data) => {
                    try {
                        const userData = await userModel.findById(data).exec();
                        const permissionData = await permissionModel.findById(userData.permissionId).exec();
                        const departmentData = await departmentModel.findById(userData.departmentId).exec();
                        if (userData){
                            console.log(data.buffer);
                            var buf = new Buffer(data.buffer.replace(/^data:image\/\w+;base64,/, ""),'base64');
                            var imageUrl = './database/image/log/' + userData.name + userData.id +'/' + Date.now(); 
                            fs.writeFile(imageUrl, buf);

                            const detetedData = faceRegconitionHeper.recognize(imageUrl);
                            console.log(detetedData);

                            // logModel.create({name: userData.name, permission: permissionData.permission, department: departmentData.name, imageUrl, detected: detetedData});
                            callback(CODE_SUCCESS +`;${MESSAGE_SUCCESS}`);
                        }
                        else {
                            callback(CODE_ERROR +`;Wrong data`);
                        }
                    }catch (error){
                        console.log(error);
                        callback(CODE_ERROR + `;Database error`);
                    }
                }
            };
            responseCode[code](socketList, socket, code, data);
        }
    });
    socket.on('disconnect', function(){
        _.omit(socketList, socket.id );
        console.log('Raps disconnected');
    })
});

function emitOpenDoor(doorId) {
    const socket = _.get(socketList, 'id.doorId', doorId);
    const code = CODE_REQUEST_OPEN_DOOR;
    socket.emit('event', code + `;Open door ${doorId}`);
}
function emitCloseDoor(doorId) {
    const socket = _.get(socketList, 'id.doorId', doorId);
    const code = CODE_REQUEST_CLOSE_DOOR;
    socket.emit('event', code + `;Close door ${doorId}`);
}
function emitLockDoor(doorId) {
    const socket = _.get(socketList, 'id.doorId', doorId);
    const code = CODE_REQUEST_LOOK_DOOR;
    socket.emit('event', code + `;Look door ${doorId}`);
}

// doorModel.create({name: 'floor 1'});
// departmentModel.create({name: "department 2"});
// permissionModel.create({name: "Nhan vien", permission: "100"});
// permissionModel.create({name: "Giam doc", permission: "200"});
// userModel.create({name: "Vong ml", departmentId: "5c12a6ddd836735497b332b6", permissionId: "5c12a76b9662e0555d62dc15"});
// userModel.create({name: "Khanh ml", departmentId: "5c12a6d568b8cc5473addd25", permissionId: "5c12a76b9662e0555d62dc16"});
// logModel.create({name: "log 1", department: "Department 1", permission: "200"});

// io.emit('some event', { for : 'everyone'});

http.listen(process.env.PORT, function(){
    console.log(`listening on  *:${process.env.PORT}`);
});

module.exports = {
    emitOpenDoor,
    emitCloseDoor,
    emitLockDoor
}
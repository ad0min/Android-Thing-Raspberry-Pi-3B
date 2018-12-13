require('dotenv').config();
const mongoose = require('mongoose');
const { Schema, SchemaType } = mongoose;
require('./mongooseConnection');
const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const _ = require('lodash');
var fs = require('fs');

const modelType = require('./models/type');
const doorModel = mongoose.model(modelType.doorType);
const userModel = mongoose.model(modelType.userType);
const logModel = mongoose.model(modelType.logType);
const departmentModel = mongoose.model(modelType.departmentType);
const permissionModel = mongoose.model(modelType.permissionType);

const {CODE_REQUEST_UNLOCK_DOOR, CODE_AUTHEN, CODE_REQUEST_CLOSE_DOOR, CODE_ERROR, CODE_REQUEST_LOCK_DOOR, CODE_REQUEST_OPEN_DOOR, CODE_RASP_INFO, CODE_CHECK_PERMISION_CARD, CODE_REQUEST_RASP_INFO, CODE_REQUEST_TAKE_PICTURE, CODE_REQUEST_WRITE_CARD, CODE_SUCCESS, MESSAGE_SUCCESS} = require('./const');

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
        // console.log(msg);
        const [code, data, error, ...opts] = msg.split(';');
        // console.log(socketList);
        if (!socketList[socket.id].authen) {
            if(code === CODE_AUTHEN){
                const res = process.env.SERVER_KEY_AUTHEN === data;
                socketList[socket.id].authen = res;
                callback(res? CODE_SUCCESS + `;${MESSAGE_SUCCESS}` : CODE_ERROR);
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
                            _.set(socketList, `${socket.id}.doorId`, data);
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
                        data = JSON.parse(data);
                        // console.log(data);
                        const userData = await userModel.findById(data.id).exec();
                        const permissionData = await permissionModel.findById(userData.permissionId).exec();
                        const departmentData = await departmentModel.findById(userData.departmentId).exec();
                        if (userData){
                            //   console.log(data.buffer);
                            var buf = new Buffer(data.buffer.replace(/^data:image\/\w+;base64,/, ""),'base64');
                            var imageUrl = './public/image/log/' + userData.name + userData.id + '_' + Date.now(); 
                            console.log('Image log url',imageUrl);
                            fs.writeFile(imageUrl, buf,(err)=>{
                                console.log('Write file result',err);
                            });
                            logModel.create({name: userData.name, permission: permissionData._id, department: departmentData._id, imageUrl});
                            callback(CODE_SUCCESS +`;${MESSAGE_SUCCESS}`);
                        }
                        else {
                            callback(CODE_ERROR +`;Wrong data`);
                        }
                    }catch (error){
                        console.log(error);
                        callback(CODE_ERROR + `;Database error`);
                    }
                },
                '200':async (socketList, socket, code, data) => {
                    if(data == CODE_REQUEST_WRITE_CARD){
                        console.log('Write card successfull');
                    }
                },
                '500': async (socketList, socket, code, data) => {
                    if(data == CODE_REQUEST_WRITE_CARD){
                        console.log('Write card error');
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
    for(let key in socketList){
        if(socketList[key].doorId == doorId){
            console.log('Emited open door ' + doorId);
            const code = CODE_REQUEST_OPEN_DOOR;
            socketList[key].socket.emit('event', code + `;Open door ${doorId}`);
        }
    }
}
function emitCloseDoor(doorId) {
    for(let key in socketList){
        if(socketList[key].doorId == doorId){
            console.log('Emited close door ' + doorId);
            const code = CODE_REQUEST_CLOSE_DOOR;
            socketList[key].socket.emit('event', code + `;Close door ${doorId}`);
        }
    }
}
function emitLockDoor(doorId) {
    for(let key in socketList){
        if(socketList[key].doorId == doorId){
            const code = CODE_REQUEST_LOCK_DOOR;
            console.log('Emited lock door ' + doorId);
            socketList[key].socket.emit('event', code + `;Look door ${doorId}`);
        }
    }
}

function emitUnlockDoor(doorId) {
    for(let key in socketList){
        if(socketList[key].doorId == doorId){
            console.log('Emited unlock door ' + doorId);
            const code = CODE_REQUEST_UNLOCK_DOOR;
            socketList[key].socket.emit('event', code + `;Unlock door ${doorId}`);
        }
    }
}

function emitWriteToCard(doorId,data) {
    let success = false;
    for(let key in socketList){
        if(socketList[key].doorId == doorId){
            console.log('Emited write to card ' + doorId + ' - ' + data);
            const code = CODE_REQUEST_WRITE_CARD;
            socketList[key].socket.emit('event', code + `;${data}`);
            success = true;
        }
    }
    return success;
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
    emitLockDoor,
    emitUnlockDoor,
    emitWriteToCard
}
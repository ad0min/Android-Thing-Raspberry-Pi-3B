require('dotenv').config();
const mongoose = require('mongoose');
const { Schema, SchemaType } = mongoose;
require('./mongooseConnection');
const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http);

app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});

io.on('connection', function(socket){
    socket.on('chat message', function(msg){
        console.log('> ' + msg);
    });
    console.log('user  connected');
    socket.send('DKM');
    socket.on('disconnect', function(){
        console.log('user disconnected');
    })
});
io.emit('some event', { for : 'everyone'});

http.listen(3000, function(){
    console.log('listening on  *:3000');
});
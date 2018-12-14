const mongoose = require('mongoose');
const modelType = require('./models/type');
const doorModel = mongoose.model(modelType.doorType);
const userModel = mongoose.model(modelType.userType);
const logModel = mongoose.model(modelType.logType);
const departmentModel = mongoose.model(modelType.departmentType);
const permissionModel = mongoose.model(modelType.permissionType);

function getLogs() {
    return logModel.find();
}

function getPersons() {
    return userModel.find();
}

function getPerson(id){
    return userModel.find({_id: id});
}

function deletePerson(id) {
    return userModel.deleteOne({_id:id});
}

function addPerson(person) {
    return userModel.create(person);
}

function getDepartment(){
    return departmentModel.find();
}
function addDepartment(department){
    return departmentModel.create(department);
}

function deleteDepartment(id) {
    return departmentModel.deleteOne({_id: id});
}

function getDoor(){
    return doorModel.find();
}
function addDoor(door){
    return doorModel.create(door);
}

function deleteDoor(id) {
    return doorModel.deleteOne({_id: id});
}

function getPermission(){
    return permissionModel.find();
}
function addPermission(permission){
    return permissionModel.create(permission);
}

function deletePermission(id) {
    return permissionModel.deleteOne({_id:id});
}


module.exports = {
    getLogs,
    getPersons,
    getDepartment,
    getPermission,
    getDoor,
    addPerson,
    addDepartment,
    addPermission,
    addDoor,
    deletePerson,
    deleteDepartment,
    deletePermission,
    deleteDoor,
    getPerson
}
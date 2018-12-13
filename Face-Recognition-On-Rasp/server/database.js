const mongo = require('mongodb');
const MongoClient = mongo.MongoClient
const HOST = 'mongodb://localhost:27017/';

const DB_NAME = 'Raspberry_PI';
const PERSON_TABLE = 'Person';
const LOG_TABLE = 'Log';
const DEPARTMENT_TABLE = 'Department';
const DOOR_TABLE = 'Door';
const PERMISSION_TABLE = 'Permission';

/**
 * Connect to database.
 */
function connect() {
    return new Promise((resolve, reject) => {
        MongoClient.connect(HOST, (err, client) => {
            if (err) {
                reject(err);
                return;
            }

            resolve(client);
        });
    });
}

/**
 * Get all provinces that was supported
 */
function getLogs() {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(LOG_TABLE).find().toArray((err, result) => {
                client.close();

                if (err) {
                    reject(err);
                    return;
                }
                result = result.map(item => {
                    item.id = item._id;
                    item._id = undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err => {
                reject(err);
            });
    });

}


function addLogs(logs) {
    console.log(logs);
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(LOG_TABLE).insertMany(logs)
                .then(result => {
                    client.close()
                    resolve();
                })
                .catch(err => {
                    client.close();
                    reject(err);
                });
        });
    });
}

function getPersons() {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(PERSON_TABLE).find().toArray((err, result) => {
                client.close();

                if (err) {
                    reject(err);
                    return;
                }
                result = result.map(item => {
                    item.id = item._id;
                    item._id = undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err => {
                reject(err);
            });
    });
}

function deletePerson(id) {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            var o_id = new mongo.ObjectID(id);
            db.collection(PERSON_TABLE).deleteOne({ _id: o_id }, (err, obj) => {
                if (err) {
                    reject(err);
                    return;
                }

                resolve(obj);
            });
        });
    });
}

function addPerson(person) {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(PERSON_TABLE).insertOne(person)
                .then(result => {
                    client.close()
                    resolve();
                })
                .catch(err => {
                    client.close();
                    reject(err);
                });
        });
    });
}

function getDepartment(){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(DEPARTMENT_TABLE).find().toArray((err, result) => {
                client.close();

                if (err) {
                    reject(err);
                    return;
                }
                result = result.map(item => {
                    item.id = item._id;
                    item._id = undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err => {
                reject(err);
            });
    });
}
function addDepartment(department){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(DEPARTMENT_TABLE).insertOne(department)
                .then(result => {
                    client.close()
                    resolve();
                })
                .catch(err => {
                    client.close();
                    reject(err);
                });
        });
    });
}

function deleteDepartment(id) {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            var o_id = new mongo.ObjectID(id);
            db.collection(DEPARTMENT_TABLE).deleteOne({ _id: o_id }, (err, obj) => {
                if (err) {
                    reject(err);
                    return;
                }

                resolve(obj);
            });
        });
    });
}

function getDoor(){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(DOOR_TABLE).find().toArray((err, result) => {
                client.close();

                if (err) {
                    reject(err);
                    return;
                }
                result = result.map(item => {
                    item.id = item._id;
                    item._id = undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err => {
                reject(err);
            });
    });
}
function addDoor(door){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(DOOR_TABLE).insertOne(door)
                .then(result => {
                    client.close()
                    resolve();
                })
                .catch(err => {
                    client.close();
                    reject(err);
                });
        });
    });
}

function deleteDoor(id) {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            var o_id = new mongo.ObjectID(id);
            db.collection(DOOR_TABLE).deleteOne({ _id: o_id }, (err, obj) => {
                if (err) {
                    reject(err);
                    return;
                }

                resolve(obj);
            });
        });
    });
}

function getPermission(){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(PERMISSION_TABLE).find().toArray((err, result) => {
                client.close();

                if (err) {
                    reject(err);
                    return;
                }
                result = result.map(item => {
                    item.id = item._id;
                    item._id = undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err => {
                reject(err);
            });
    });
}
function addPermission(permission){
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(PERMISSION_TABLE).insertOne(permission)
                .then(result => {
                    client.close()
                    resolve();
                })
                .catch(err => {
                    client.close();
                    reject(err);
                });
        });
    });
}

function deletePermission(id) {
    return new Promise((resolve, reject) => {
        connect().then(client => {
            const db = client.db(DB_NAME)
            var o_id = new mongo.ObjectID(id);
            db.collection(PERMISSION_TABLE).deleteOne({ _id: o_id }, (err, obj) => {
                if (err) {
                    reject(err);
                    return;
                }

                resolve(obj);
            });
        });
    });
}
        
function getDataWithCondition(tb_name,condition){
    return new Promise((resolve, reject) =>{
        connect().then(client => {
            const db = client.db(DB_NAME)
            db.collection(tb_name).find(condition).toArray((err,result)=>{
                client.close();
                if(err){
                    reject(err);
                    return;
                }
                result = result.map(item =>{
                    item.id = item._id;
                    item._id=undefined;
                    return item;
                })
                resolve(result);
            });
        })
            .catch(err=>{
                reject(err);
            })
    });
}

module.exports = {
    getLogs,
    getPersons,
    getDepartment,
    getPermission,
    getDoor,
    addPerson,
    addLogs,
    addDepartment,
    addPermission,
    addDoor,
    deletePerson,
    deleteDepartment,
    deletePermission,
    deleteDoor,
    getDataWithCondition
}
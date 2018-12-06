const MongoClient = require('mongodb').MongoClient
const HOST = 'mongodb://localhost:27017/';

const DB_NAME = 'Raspberry_PI';
const PERSON_TABLE = 'Person';
const LOG_TABLE = 'Log';

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

module.exports = {
            getLogs,
            getPersons,
            addPerson
        }
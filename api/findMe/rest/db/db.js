var mysql = require ('mysql');

let config = require('./config');

let db = mysql.createConnection(config);

module.exports = db;

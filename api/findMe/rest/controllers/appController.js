var bodyParser = require('body-parser');
module.exports = (app) => {
    app.get('/', (req, res, next) => {
        console.log("test", "test called");
        res.status(200).json({test: "it works mothafuckas"})
    });

    app.post('/id', (req, res, next) => {
        console.log("test", req);
        let postquery = 'CALL getuid();';
        const values = null;
        let uid;
        app.db.query(postquery, function (err, result) {
            if (err) throw err;
            uid = result[0][0].uid;
            let postQuery = 'INSERT INTO uloc (uid, longitude, latitude) VALUES(?, ?, ?);';
            let response = req;
            let params = [
                uid,
                response.param("longi", 0),
                response.param("lat", 0)
            ];
            app.db.query(postQuery, params, function (err, result) {
                if (err) throw err;
            });
            res.status(200).json(uid);
        });
    });

    app.post('/', (req, res, next )=> {

        let postQuery = 'INSERT INTO uloc (uid, longitude, latitude) VALUES(?, ?, ?)'
        let response = req;
        let params = [
            response.param("longi", 0),
            response.param("lat", 0)
        ];
        app.db.query(postQuery, params, function (err, result) {
            if (err) throw err;
            res.status(201).json("Added succesfully")
        } )
    });

};
module.exports = (app) => {
    app.get('/', (req, res, next) => {
        res.status(200).json("It works mothafockas")
    });

    app.get('/id', (req, res, next) => {

        let postquery = 'CALL getuid();';
        const values = null;
        let uid;
        app.db.query(postquery, function (err, result) {
            if (err) throw err;
            uid = result[0][0].uid;
            let postQuery = 'INSERT INTO uloc (uid, longitude, latitude) VALUES(?, ?, ?);'
            let params = [
                uid,
                req.body.longi,
                req.body.lat
            ];
            app.db.query(postQuery, params, function (err, result) {
                if (err) throw err;
            });
            res.status(200).json(uid);
        });
    });

    app.post('/', (req, res, next )=> {
        let postQuery = 'INSERT INTO uloc (uid, longitude, latitude) VALUES(?, ?, ?)'
        let params = [
            req.body.id,
            req.body.longi,
            req.body.lat
        ];
        app.db.query(postQuery, params, function (err, result) {
            if (err) throw err;
            res.status(201).json("Added succesfully")
        } )
    });

};
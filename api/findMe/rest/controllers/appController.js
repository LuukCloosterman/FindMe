var bodyParser = require('body-parser');
module.exports = (app) => {
    app.get('/', (req, res, next) => {
        console.log("test", "test called");
        res.status(200).json({test: "it works mothafuckas"})
    });

    app.post('/id', (req, res, next) => {
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
            console.log("posted: " + params);
            res.status(200).json(uid);
        });
    });

    app.post('/', (req, res, next )=> {
        let userid = req.param("id");
        let longi = req.param("longi");
        let lat = req.param("lat");
        let postQuery = 'UPDATE uloc SET longitude = ' + longi + " , latitude = " + lat + " WHERE uid= " + userid + ";";
        app.db.query(postQuery, function (err, result) {
            console.log("posted", postQuery);
            if (err) throw err;
            res.status(201).json("Updated succesfully")
        } )
    });
    app.get('/getgoto', (req,res, next)=>{
        console.log(req.toString());
        let userid = req.param("id");
        let lat = req.param("lat");
        let longi = req.param("longi");
        if(userid!=undefined) {
             console.log("weer een stapje verder");
            let makequery = 'CALL makeGemLoc(' + userid + ", " + lat + ", " + longi + ' );'
            let getquery = 'CALL getgemloc(' + userid + ');'
            app.db.query(getquery, function (err, result) {
                if (err) throw err;
                if (!result[0].entries([])) {
                    res.status(200).json(result[0]);
                    console.log(result[0]);
                } else {
                    app.db.query(makequery, function (err, result) {
                        if (err) {
                            console.log(err);
                        }
                        console.log("makeresult", result);
                        app.db.query(getquery, function (err, result) {
                            if (result[0][0]) {
                                res.status(200).json(result[0][0]);
                            } else {
                                res.status(200).json({"messsage": "no point found yet"})
                            }
                        })
                    })
                }
            })
        }
        else {
            console.log("undefined shits")
        }
    });
    app.delete('/', (req, res, next)=>{
       let userid = req.param("id");
    });


};
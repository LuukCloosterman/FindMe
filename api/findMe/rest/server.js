var express     = require('express');
var bodyParser  = require('body-parser');

let app         = express(); // Please do not remove this line, since CLI uses this line as guidance to import new controllers
let db          = require('./db/db');
app.db          = db;
app.db.connect(function(err){

  if(err) throw err;
  console.log("connected")


});
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
require('./controllers/appController')(app);
app.listen(process.env.PORT || 3000, () => {
  console.log('Server is running');
});


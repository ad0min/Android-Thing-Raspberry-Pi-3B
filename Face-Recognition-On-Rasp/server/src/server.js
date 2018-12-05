var express = require('express');
var app = express();
var bodyParser = require('body-parser');

var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/";



function connectMongo(callback){
	MongoClient.connect(url, function(err, db) {
	  if (err) throw err;
	  var dbo = db.db("rapbery");
	  dbo.collection("users").find({}).toArray(function(err, result) {
	    if (err) throw err;
	    // console.log(result[0].username);
	    db.close();
	    callback(result);
	  });
	});	
}
// Tao mot parser co dang application/x-www-form-urlencoded
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.use(express.static('public'));

app.post('/check_login', urlencodedParser, function (req, res) {
   // console.log(req.body.first_name);
   // console.log(req.body.last_name);
   connectMongo(function(result){
   	console.log(result);	
   })
   
   // if(req.body.first_name==result[0].username && req.body.last_name==result[0].password){
   // 	return res.redirect('/login');
   // }
})

app.get('/login',function(req,res){
	res.send('Hello world');
})

app.get('/', function (req, res) {
	res.sendFile( __dirname + "/" + "index.html" );
})

var server = app.listen(80, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})


var express = require('express');
var app = express();
var bodyParser = require('body-parser');

var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://192.168.1.23:27017/";

var checklogin = false;

function dataArrayMongo(callback, db_name, collection_name) {
	MongoClient.connect(url, function (err, db) {
		if (err) throw err;
		var dbo = db.db(db_name);
		dbo.collection(collection_name).find({}).toArray(function (err, result) {
			if (err) throw err;
			db.close();
			callback(result);
		});
	});
}
// Tao mot parser co dang application/x-www-form-urlencoded
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.use(express.static('public'));
app.set('view engine', 'ejs')

app.post('/check_login', urlencodedParser, function (req, res) {
	db_name = "rapbery";
	collection_name = "users";
	dataArrayMongo(function (result) {
		let leng = result.length;
		let i;
		for (i = 0; i < leng; i++) {
			if (req.body.username == result[i].username && req.body.password == result[i].password) {
				checklogin = true;
				return res.redirect('/home');
			}
		}
		return res.redirect('/')
	}, db_name, collection_name);
})


app.get('/home', function (req, res) {
	res.sendFile(__dirname + "/views/home.html");
})

app.get('/log',function(req,res){
	const log = [
		{
			date: "04/12/2018",
			time: "12:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		},
		{
			date: "04/12/2018",
			time: "12:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		},
		{
			date: "04/12/2018",
			time: "12:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		},
		{
			date: "04/12/2018",
			time: "12:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		},{
			date: "04/12/2018",
			time: "12:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		}
	]

	res.render("pages/log.ejs",{logs:log});
});

app.get('/user',function(req,res){

	res.sendFile(__dirname + "/views/user.html");
});

app.post('/logout', function (req, res) {
	checklogin = false;
	return res.redirect('/');
})
app.get('/', function (req, res) {
	res.sendFile(__dirname + "/views/index.html");
})

var server = app.listen(80, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})


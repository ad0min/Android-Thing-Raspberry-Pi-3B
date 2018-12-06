var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var fs = require('fs');
var formidable = require('formidable');
const database = require('./database');

// Tao mot parser co dang application/x-www-form-urlencoded
var urlencodedParser = bodyParser.urlencoded({ extended: false })
// app.use(urlencodedParser);
app.use(express.json());

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
	res.render("home");
})

app.get('/log', function (req, res) {
	database.getLogs().then(result => {
		result = result.map((item) => {
			var date = new Date(item.timestamp);
			// Hours part from the timestamp
			var hours = date.getHours();
			// Minutes part from the timestamp
			var minutes = "0" + date.getMinutes();
			// Seconds part from the timestamp
			var seconds = "0" + date.getSeconds();
			// Will display time in 10:30:23 format
			item.time = hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);

			var day = '0' + date.getDay();
			var month = '0' + date.getMonth();
			var year = date.getFullYear();
			item.date = day.slice(-2) + '/' + month.slice(-2) + '/' + year;

			return item;
		});
		res.render("log", { logs: result });
	}).catch(err => {

	});
});

app.post('/add-log', bodyParser.raw(), (req, res) => {
	database.addLogs(req.body).then(success => {
		res.status(200).send();
	}).catch(err => {
		console.log(err);
		res.status(404).send(err);
	});
});

app.get('/user', function (req, res) {
	database.getPersons().then(result => {
		res.render("user", { users: result });
	})
		.catch(err => {
			console.log(err);
			res.status(400).send();
		});
});

app.get('/delete-person', (req,res)=>{
	const id = req.query.id;
	console.log('delete person',id);
	database.deletePerson(id).then(result=>{
		console.log(result);
		res.redirect('/user');
	})
	.catch(err=>{
		console.log(err);
		res.redirect('/user');
	})
});

app.post('/logout', function (req, res) {
	checklogin = false;
	return res.redirect('/');
})

app.get('/add-person', (req, res) => {
	res.render('add-user');
});
app.get('/', function (req, res) {
	res.render("index");
})

app.post('/add-person', (req, res) => {
	var form = new formidable.IncomingForm();
	form.multiples = true;
	//Thiết lập thư mục chứa file trên server
	form.uploadDir = "public/upload/";
	//xử lý upload
	form.parse(req, function (err, fields, files) {
		console.log(files);
		try {
			fields.images = [];

			if (Array.isArray(files.images)) {
				for (let i = 0; i < files.images.length; i++) {
					const file = files.images[i];
					//path tmp trên server
					var path = file.path;
					let userName = fields.name.replace(/ /g, '_')
					let fileName = userName + '-' +
						new Date().getTime() + '-' + file.name;
					//thiết lập path mới cho file
					var newpath = form.uploadDir + fileName;
					fields.images.push('/upload/' + fileName);
					fs.rename(path, newpath, function (err) {
						if (err) throw err;
					});
				}
			} else {
				const file = files.images;
				//path tmp trên server
				var path = file.path;
				let userName = fields.name.replace(/ /g, '_')
				let fileName = userName + '-' +
					new Date().getTime() + '-' + file.name;
				//thiết lập path mới cho file
				var newpath = form.uploadDir + fileName;
				fields.images.push('/upload/' + fileName);
				fs.rename(path, newpath, function (err) {
					if (err) throw err;
				});
			}


			fields.status = parseInt(fields.status);
			// console.log(fields);
			database.addPerson(fields);
			res.redirect('/user');
		} catch (err) {
			res.status(403).send();
		}
	});
});

var server = app.listen(80, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})


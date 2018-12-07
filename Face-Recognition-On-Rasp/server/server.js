var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var fs = require('fs');
var formidable = require('formidable');
const database = require('./database');
var session = require('express-session')

// Tao mot parser co dang application/x-www-form-urlencoded
var urlencodedParser = bodyParser.urlencoded({ extended: false })

// app.use(urlencodedParser);
app.use(session({
	secret: 'work hard raspberry',
	resave: true,
	saveUninitialized: false
  }));
app.use(express.json());

app.use(express.static('public'));
app.set('view engine', 'ejs');


app.use((req,res,next)=>{
	if(req.originalUrl == '/login'){
		next();
	} else if(req.session.userId){
		next();
	} else{
		res.redirect('/login');
	}
})

app.post('/login', urlencodedParser, function (req, res) {
	console.log(req.body);
	if ((req.body.username == 'admin' && req.body.password == 'admin') ||
	(req.body.username == 'ad' && req.body.password == 'ad')) {
		const session = req.session;
		session.userId = req.body.username;
		return res.redirect('/home');
	} else{
		return res.redirect('/');
	}
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

app.get('/card', function (req, res) {
	database.getPersons().then(result => {
		res.render("card", { users: result });
	})
		.catch(err => {
			console.log(err);
			res.status(400).send();
		});
});

app.get('/rapsberry',function (req,res){
	res.render('rapsberry'); //need to edit later
})


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
	req.session.userId = undefined;
	return res.redirect('/login');
})

app.get('/add-person', (req, res) => {
	res.render('add-user');
});

app.get('/add-raps',(req,res) => {
	res.render('add-raps')
})
app.get('/', function (req, res) {
	res.redirect('/home');
})

app.get('/login',(req,res)=>{
	console.log(req.session);
	if(req.session.userId){
		res.redirect('/home');
	} else{
		res.render('index');
	}
});

app.post('/add-card', (req, res) => {
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
			res.redirect('/card');
		} catch (err) {
			res.status(403).send();
		}
	});
});

var server = app.listen(8080, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})


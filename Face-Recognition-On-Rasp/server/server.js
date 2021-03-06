require('dotenv').config();
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var fs = require('fs');
var formidable = require('formidable');

require('./mongooseConnection');
const database = require('./database');
var session = require('express-session')
const socketManager = require('./index');


// Tao mot parser co dang application/x-www-form-urlencoded
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.use(urlencodedParser);
app.use(session({
	secret: 'work hard raspberry',
	resave: true,
	saveUninitialized: false
}));
app.use(express.json());

app.use(express.static('public'));
app.set('view engine', 'ejs');


app.use((req, res, next) => {
	if (req.originalUrl == '/login') {
		next();
	} else if (req.session.userId) {
		next();
	} else {
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
	} else {
		return res.redirect('/');
	}
})


app.get('/home', function (req, res) {
	return res.render('home')
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

			item.imageUrlDetected = item.imageUrl.replace('log', 'detected');

			return item;
		});
		console.log('Time',JSON.stringify(result[result.length-1]));
		// console.log('Log data',result);
		res.render("log", { logs: result });
	}).catch(err => {

	});
});

// app.post('/add-log', bodyParser.raw(), (req, res) => {
// 	// database.addLogs(req.body).then(success => {
// 	// 	res.status(200).send();
// 	// }).catch(err => {
// 	// 	console.log(err);
// 	// 	res.status(404).send(err);
// 	// });
// });

app.get('/user', function (req, res) {
	const id = req.query.userId;
	let dataPromise;
	if(id){
		dataPromise = database.getPerson(id);
	} else{
		dataPromise = database.getPersons();
	}
	dataPromise.then(users => {
		database.getPermission().then(permissions => {
			database.getDepartment().then(departments => {
				database.getDoor().then(doors => {
					for (let i = 0; i < users.length; i++) {
						const user = users[i];
						for (let m = 0; m < departments.length; m++) {
							if (user.departmentId == departments[m].id) {
								user.departmentName = departments[m].name;
								break;
							}
						}
						for (let n = 0; n < permissions.length; n++) {
							if (user.permissionId == permissions[n].id) {
								user.permissionLevel = permissions[n].name;
								break;
							}
						}
					}
					for(let i = 0;i< doors.length;i++){
						for(let j = 0;j< departments.length;j++){
							if(doors[i].departmentId == departments[j].id){
								doors[i].departmentName = departments[j].name;
								break;
							}
						}
					}

					console.log("render result user 1: ", users);
					res.render("user", {
						users: users,
						permissions: permissions,
						departments: departments,
						doors: doors,
						status: req.query.status,
						message: req.query.action + ' ' + req.query.status
						
					});
				});
			})
		})

	})
		.catch(err => {
			console.log(err);
			res.status(400).send();
		});
});

app.get('/add-user', (req, res) => {
	database.getDepartment().then(department => {
		database.getPermission().then(permission => {
			res.render('add-user', { departments: department, permissions: permission });
		})
	})
		.catch(err => {
			console.log(err);
			res.status(400).send();
		})
	// res.render('add-user');
});

app.post('/add-user', (req, res) => {
	console.log("chay chua m");
	var form = new formidable.IncomingForm();
	form.multiples = true;
	//Thiết lập thư mục chứa file trên server
	form.uploadDir = "public/upload/";
	//xử lý upload
	form.parse(req, function (err, fields, files) {
		// console.log(files);
		try {
			fields.avatar = '';

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
				console.log("if vong dep trai");
			} else {
				const file = files.images;
				//path tmp trên server
				var path = file.path;
				let userName = fields.name.replace(/ /g, '_')
				let fileName = userName + '-' +
					new Date().getTime() + '-' + file.name;
				//thiết lập path mới cho file
				var newpath = form.uploadDir + fileName;
				fields.avatar = '/upload/' + fileName;
				fs.rename(path, newpath, function (err) {
					if (err) throw err;
				});
				console.log("else: vong dep trai");
			}

			database.addPerson(fields);
			console.log("data insert user: ", fields);
			res.redirect('/user');
		} catch (err) {
			console.log(err);
			res.status(403).send();
		}
	});
});
app.get('/delete-person', (req, res) => {
	const id = req.query.id;
	console.log('delete person', id);
	database.deletePerson(id).then(result => {
		console.log(result);
		res.redirect('/user');
	})
		.catch(err => {
			console.log(err);
			res.redirect('/user');
		})
});


app.get('/department', function (req, res) {
	database.getDepartment().then(result => {

		res.render('department', { departments: result }); //need to edit later	
	})

})
app.get('/add-department', (req, res) => {
	res.render('add-department')
})
app.post('/add-department', (req, res) => {
	console.log("department chay chua m");
	var form = new formidable.IncomingForm();
	form.multiples = true;
	//Thiết lập thư mục chứa file trên server
	// form.uploadDir = "public/upload/";
	//xử lý upload
	form.parse(req, function (err, fields) {
		// console.log(files);
		try {
			database.addDepartment(fields);
			console.log("data insert user: ", fields);
			res.redirect('/department');
		} catch (err) {
			console.log(err);
			res.status(403).send();
		}
	});
});
app.get('/delete-department', (req, res) => {
	const id = req.query.id;
	console.log('delete department', id);
	database.deleteDepartment(id).then(result => {
		console.log(result);
		res.redirect('/department');
	})
		.catch(err => {
			console.log(err);
			res.redirect('/department');
		})
});

app.get('/door', function (req, res) {
	database.getDoor().then(door => {
		database.getDepartment().then(department => {
			database.getPermission().then(permission => {
				res.render('door', {
					doors: door,
					departments: department,
					permissions: permission,
					doorId: req.query.id,
					message: req.query.action + ' ' + req.query.status
				});
			})
		})
	})
})

app.get('/add-door', function (req, res) {
	database.getDepartment().then(department => {
		database.getPermission().then(permission => {
			res.render('add-door', { departments: department, permissions: permission });
		})
	})
		.catch(err => {
			console.log(err);
			res.status(400).send();
		})
})
app.post('/add-door', (req, res) => {
	console.log("add door chay chua m");
	var form = new formidable.IncomingForm();
	form.multiples = true;

	form.parse(req, function (err, fields) {

		try {
			database.addDoor(fields);
			console.log("data insert user: ", fields);
			res.redirect('/door');
		} catch (err) {
			console.log(err);
			res.status(403).send();
		}
	});
});

app.get('/delete-door', (req, res) => {
	const id = req.query.id;
	console.log('delete door', id);
	database.deleteDoor(id).then(result => {
		console.log(result);
		res.redirect('/door');
	})
		.catch(err => {
			console.log(err);
			res.redirect('/door');
		})
});

app.get('/permission', function (req, res) {
	database.getPermission().then(result => {
		console.log(result);
		res.render('permission', { permissions: result });
	})
	// res.render('permission'); //need to edit later
})

app.get('/add-permission', function (req, res) {
	res.render('add-permission'); //need to edit later
})

app.post('/add-permission', (req, res) => {
	console.log("add permission chay chua m");
	var form = new formidable.IncomingForm();
	form.multiples = true;

	form.parse(req, function (err, fields) {
		console.log("data insert permission: ", fields);
		try {
			database.addPermission(fields).then(result => {
				res.redirect('/permission');
			})
				.catch(err => {
					res.redirect('/permission');
				});

		} catch (err) {
			console.log(err);
			res.status(403).send();
		}
	});
});
app.get('/delete-permission', (req, res) => {
	const id = req.query.id;
	console.log('delete permission', id);
	database.deletePermission(id).then(result => {
		console.log(result);
		res.redirect('/permission');
	})
		.catch(err => {
			console.log(err);
			res.redirect('/permission');
		})
});

app.post('/logout', function (req, res) {
	req.session.userId = undefined;
	return res.redirect('/login');
})




app.get('/', function (req, res) {
	res.redirect('/home');
})

app.get('/login', (req, res) => {
	console.log(req.session);
	if (req.session.userId) {
		res.redirect('/home');
	} else {
		res.render('index');
	}
});

app.get('/door/open', (req, res) => {
	const params = req.query;
	const id = params["id"];
	socketManager.emitOpenDoor(id);
	res.redirect('/door?action=Open door&status=success&id=' + id);
});

app.get('/door/close', (req, res) => {
	const params = req.query;
	const id = params["id"];
	socketManager.emitCloseDoor(id);
	res.redirect('/door?action=Close door&status=success&id=' + id);
});

app.get('/door/lock', (req, res) => {
	const params = req.query;
	const id = params["id"];
	socketManager.emitLockDoor(id);
	res.redirect('/door?action=Lock door&status=success&id=' + id);
});

app.get('/door/unlock', (req, res) => {
	const params = req.query;
	const id = params["id"];
	socketManager.emitUnlockDoor(id);
	res.redirect('/door?action=Unlock door&status=success&id=' + id);
});

app.get('/door/take-picture', (req, res) => {
	const params = req.query;
	const id = params.id;
	console.log('Call take picture from door id ' + id);
	socketManager.emitTakePicture(id,(url)=>{
		res.redirect(url);
	});
});

app.post('/write-user', (req, res) => {
	var form = new formidable.IncomingForm();
	form.parse(req, function (err, fields) {
		try {
			console.log("data write to card: ", fields);
			let success = socketManager.emitWriteToCard(fields['doorId'],fields['userId']);
			let status = success?'success':'error';
			res.redirect('/user?action=Write data to card&status=' + status);
		} catch (err) {
			console.log(err);
			res.status(403).send();
		}
	});
});



var server = app.listen(3000, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})
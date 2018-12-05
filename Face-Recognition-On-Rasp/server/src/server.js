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
	res.render("home");
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
			time: "13:03:00",
			username: "Duong Vong",
			userId: 12,
			status: 2,
			image: "link",
		},
		{
			date: "04/12/2018",
			time: "20:03:00",
			username: "Khanh Tran",
			userId: 12,
			status: 0,
			image: "link",
		},
		{
			date: "12/12/2018",
			time: "21:03:00",
			username: "Nguyen Thanh Dat",
			userId: 12,
			status: 1,
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

	res.render("log",{logs:log});
});

app.get('/user',function(req,res){
	const user = [
		{
			id: 0,
			name: "Khanh Tran",
			images: [
				"https://www.washingtonpost.com/resizer/LUH1ZaouWY3_h0necP-XrTxa9K0=/200x200/s3.amazonaws.com/arc-authors/washpost/c68f3967-dcae-4e2b-9bea-db3ed9928896.png",
				"https://i.kinja-img.com/gawker-media/image/upload/s--Tg_qqR3r--/c_scale,f_auto,fl_progressive,q_80,w_800/dnmtn4ksijwyep0xmljk.jpg",
				"https://pbs.twimg.com/profile_images/1717956431/BP-headshot-fb-profile-photo_400x400.jpg",
				"http://img.timeinc.net/time/photoessays/2008/people_who_mattered/obama_main_1216.jpg",
				"https://www.utoronto.ca/sites/default/files/2018-11-13-dementia-resized.jpg?147056"
			],
			status: 1,
		},
		{
			id: 2,
			name: "Khanh Tran",
			images: [
				"https://www.washingtonpost.com/resizer/LUH1ZaouWY3_h0necP-XrTxa9K0=/200x200/s3.amazonaws.com/arc-authors/washpost/c68f3967-dcae-4e2b-9bea-db3ed9928896.png",
				"https://i.kinja-img.com/gawker-media/image/upload/s--Tg_qqR3r--/c_scale,f_auto,fl_progressive,q_80,w_800/dnmtn4ksijwyep0xmljk.jpg",
				"https://pbs.twimg.com/profile_images/1717956431/BP-headshot-fb-profile-photo_400x400.jpg",
				"http://img.timeinc.net/time/photoessays/2008/people_who_mattered/obama_main_1216.jpg",
				"https://www.utoronto.ca/sites/default/files/2018-11-13-dementia-resized.jpg?147056"
			],
			status: 0,
		},
		{
			id: 3,
			name: "Vong",
			images: [
				"https://www.washingtonpost.com/resizer/LUH1ZaouWY3_h0necP-XrTxa9K0=/200x200/s3.amazonaws.com/arc-authors/washpost/c68f3967-dcae-4e2b-9bea-db3ed9928896.png",
				"https://i.kinja-img.com/gawker-media/image/upload/s--Tg_qqR3r--/c_scale,f_auto,fl_progressive,q_80,w_800/dnmtn4ksijwyep0xmljk.jpg",
				"https://pbs.twimg.com/profile_images/1717956431/BP-headshot-fb-profile-photo_400x400.jpg",
				"http://img.timeinc.net/time/photoessays/2008/people_who_mattered/obama_main_1216.jpg",
				"https://www.utoronto.ca/sites/default/files/2018-11-13-dementia-resized.jpg?147056"
			],
			status: 1,
		},
		{
			id: 4,
			name: "Dat",
			images: [
				"https://www.washingtonpost.com/resizer/LUH1ZaouWY3_h0necP-XrTxa9K0=/200x200/s3.amazonaws.com/arc-authors/washpost/c68f3967-dcae-4e2b-9bea-db3ed9928896.png",
				"https://i.kinja-img.com/gawker-media/image/upload/s--Tg_qqR3r--/c_scale,f_auto,fl_progressive,q_80,w_800/dnmtn4ksijwyep0xmljk.jpg",
				"https://pbs.twimg.com/profile_images/1717956431/BP-headshot-fb-profile-photo_400x400.jpg",
				"http://img.timeinc.net/time/photoessays/2008/people_who_mattered/obama_main_1216.jpg",
				"https://www.utoronto.ca/sites/default/files/2018-11-13-dementia-resized.jpg?147056"
			],
			status: 1,
		},
	]
	res.render("user",{users: user});
});

app.post('/logout', function (req, res) {
	checklogin = false;
	return res.redirect('/');
})

app.get('/add-person',(req,res)=>{
	res.render('add-user');
});
app.get('/', function (req, res) {
	res.render("index");
})

var server = app.listen(80, function () {

	var host = server.address().address
	var port = server.address().port

	console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

})


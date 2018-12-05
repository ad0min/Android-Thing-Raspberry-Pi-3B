  var express = require('express');
  var app = express();
  var bodyParser = require('body-parser');

  var MongoClient = require('mongodb').MongoClient;
  var url = "mongodb://localhost:27017/";

  var checklogin=false;

  function dataArrayMongo(callback,db_name,collection_name){
  	MongoClient.connect(url, function(err, db) {
  	  if (err) throw err;
  	  var dbo = db.db(db_name);
  	  dbo.collection(collection_name).find({}).toArray(function(err, result) {
  	    if (err) throw err;
  	    db.close();
  	    callback(result);
  	  });
  	});	
  }
  function insertDataMongo(callback,db_name,collection_name,data,check){
    MongoClient.connect(url, function(err, db) {
      if (err) {
        check=[{status:0, error:err}]
        return;
        // throw err;
      }
      var dbo = db.db(db_name);
      dbo.collection(collection_name).insertMany(data, function(err, res) {
        if (err){ 
          check=[{status:0, error:err}]
          return
          // throw err;
        }
        else {
          check=[{status:1, error:"no error"}];
        }
        db.close();

        callback(check);
      });
    });
  }
// Tao mot parser co dang application/x-www-form-urlencoded
  var urlencodedParser = bodyParser.urlencoded({ extended: false })

  app.use(express.static('public'));

  app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

  app.post('/check_login', urlencodedParser, function (req, res) {
     db_name="rapbery";
     collection_name="users";
     dataArrayMongo(function(result){ 
        let leng=result.length;
        let i;
        for(i=0;i<leng;i++){
          if(req.body.username==result[i].username && req.body.password==result[i].password){
            checklogin=true;
            return res.redirect('/home');
          }
        }
        return res.redirect('/');
     },db_name,collection_name);
  })

  app.post('/logout',function(req,res){
    checklogin=false;
    return res.redirect('/');
  })

  app.post('/insert_user',function(req,res){
    var data={username: req.body.username, pasword: req.body.password};
    insertDataMongo("rapbery","users",data);
    return res.redirect('/');
  })

  app.get('/api/users_detected',function(req,res){
    dataArrayMongo(function(result){
      res.end(JSON.stringify(result));
    },'rapbery',"users_detected");
  })

  app.get('/api/users',function(req,res){
      dataArrayMongo(function(result){
        // print(result);
        res.end(JSON.stringify(result));
      },"rapbery","users");
  })

  app.post('/api/upload_data_detect',function(req,res){
      var check=[{status: -1, error:"null"}];
      var data=[
        {username:'duongvongdeptrai',age:'21',class:'iot'},
        {username:'tran quoc khanh quoc tran',age:'21',class:'iot'},
        {username:'nguyenthanhdatthanhnguyen',age:'22',class:'iot'}
        ];
      insertDataMongo(function(check){
        res.end(JSON.stringify(check));    
      },"rapbery","users_detected",data,check);
      
  })


  app.get('/home',function(req,res){
  	if(checklogin) {
      res.sendFile(__dirname + "/" + "home.html");
    }
    else res.redirect('/');
  })
  app.get('/register',function(req,res){
    res.sendFile(__dirname + "/" + "register.html")
  })
  
  app.get('/', function (req, res) {
  	res.sendFile( __dirname + "/" + "index.html" );
  })

  var server = app.listen(8080, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("Ung dung Node.js dang lang nghe tai dia chi: http://%s:%s", host, port)

  })


// var MongoClient = require('mongodb').MongoClient;
// var url = "mongodb://localhost:27017/";

// MongoClient.connect(url, function(err, db) {
//   if (err) throw err;
//   var dbo = db.db("rapbery");
//   var myobj = [
//     { username: 'John', password: 'Highway 71'},
//     { username: 'Peter', password: 'Lowstreet 4'},
//     { username: 'Amy', password: 'Apple st 652'},
//     { username: 'Hannah', password: 'Mountain 21'},
//     { username: 'Michael', password: 'Valley 345'},
//     { username: 'Sandy', password: 'Ocean blvd 2'},
//     { username: 'Betty', password: 'Green Grass 1'},
//     { username: 'Richard', password: 'Sky st 331'},
//     { username: 'Susan', password: 'One way 98'},
//     { username: 'Vicky', password: 'Yellow Garden 2'},
//     { username: 'Ben', password: 'Park Lane 38'},
//     { username: 'William', password: 'Central st 954'},
//     { username: 'Chuck', password: 'Main Road 989'},
//     { username: 'Viola', password: 'Sideway 1633'}
//   ];
//   dbo.collection("users").insertMany(myobj, function(err, res) {
//     if (err) throw err;
//     console.log("Number of documents inserted: " + res.insertedCount);
//     db.close();
//   });
// });
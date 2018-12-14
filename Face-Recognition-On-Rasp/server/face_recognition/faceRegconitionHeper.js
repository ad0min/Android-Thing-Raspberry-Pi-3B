const {PythonShell} = require('python-shell');

function recognize(imageUrl, callback){
  let options = {
    mode: 'text',
    scriptPath: './face_recognition',
    args: ['-d face_recognition/face_detection_model', '-m face_recognition/openface_nn4.small2.v1.t7', '-r face_recognition/output/recognizer.pickle', '-l face_recognition/output/le.pickle', `-i ${imageUrl}`]
  };
  
  PythonShell.run('recognize.py', options, function (err, results) {
    if (err) throw err;
    // results is an array consisting of messages collected during execution
    console.log('results: %j', results);
    return callback(results);
  });
}
// recognize('./public/upload/Thành_Đạt-1544754307560-2018-03-24-002852_3.jpg')


function extract_embeddings(imageTrainFolder){
  let options = {
    mode: 'text',
    pythonOptions: ['-u'], // get print results in real-time
    scriptPath: './face_recognition',
    args: [`--dataset ${imageTrainFolder}`, '--embeddings output/embeddings.pickle', '--detector face_detection_model', '--embedding-model openface_nn4.small2.v1.t7']
  };
  
  PythonShell.run('recognize.py', options, function (err, results) {
    if (err) throw err;
    // results is an array consisting of messages collected during execution
    console.log('results: %j', results);
    return results;
  });
}


function train_model(){
  let options = {
    mode: 'text',
    pythonOptions: ['-u'], // get print results in real-time
    scriptPath: './face_recognition',
    args: ['--embeddings output/embeddings.pickle', '--recognizer output/recognizer.pickle', '--le output/le.pickle']
  };
  
  PythonShell.run('train_model.py', options, function (err, results) {
    if (err) throw err;
    // results is an array consisting of messages collected during execution
    console.log('results: %j', results);
    return results;
  });
}

module.exports = {
  recognize,
  extract_embeddings,
  train_model,
}
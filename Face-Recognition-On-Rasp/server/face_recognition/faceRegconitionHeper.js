import {PythonShell} from 'python-shell';

function recognize(imageUrl){
  let options = {
    mode: 'text',
    pythonOptions: ['-u'], // get print results in real-time
    scriptPath: './face_recognition',
    args: ['--detector face_detection_model', '--embedding-model openface_nn4.small2.v1.t7', '--recognizer output/recognizer.pickle', '--le output/le.pickle', `--image ../${imageUrl}`]
  };
  
  PythonShell.run('recognize.py', options, function (err, results) {
    if (err) throw err;
    // results is an array consisting of messages collected during execution
    console.log('results: %j', results);
    return results;
  });
}


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
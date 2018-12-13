const mongoose = require('mongoose');

function init(){

}

const opts = {
  autoReconnect: true,
  reconnectTries: Number.MAX_VALUE,
  reconnectInterval: 1000,
  promiseLibrary: Promise,
};
console.log(process.env.MONGO_URI);

mongoose
  .connect(process.env.MONGO_URI, opts)
  .then(
    async () => {
      console.log(`MongoDB successfully connected to ${process.env.MONGO_URI}`);
    },
    (err) => {
      console.log(`MongoDB connection error ${err}`);
      if (err.message && err.message.code === 'ETIMEDOUT') {
        console.log('Retrying...');
        mongoose.connect(
          process.env.MONGO_URI,
          opts,
        );
      }
    }
  );

if (process.env.MONGO_DEBUG === 'true') {
  mongoose.set('debug', true);
}

require('./models');
  
const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const logSchema = Schema(
  {
		name: String, 
		imageUrl: String,
		department: {
			type: ObjectId,
			index: true,
    },
		permission: String,
		detected: [{
			name: String,
			prob: String,
		}]
	}, {
    timestamps: true,
  }
)

module.exports = mongoose.model(modelType.logType, logSchema);
const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const userSchema = Schema(
  {
		name: String, 
		avatar: String,
		deparmentId: {
			type: ObjectId,
			index: true,
		},
		permissionId: {
			type: ObjectId,
		}
	}
)

module.exports = mongoose.model(modelType.userType, userSchema);
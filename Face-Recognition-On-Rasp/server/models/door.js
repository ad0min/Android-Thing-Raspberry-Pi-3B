const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const doorSchema = Schema(
  {
		name: String, 
		avatar: String,
		departmentId: {
			type: ObjectId,
			index: true,
		},
		minLevelPermission: {
			type: ObjectId,
			index: true,
		},
	}
)

module.exports = mongoose.model(modelType.doorType, doorSchema);
const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const logSchema = Schema(
  {
		userName: String, 
		userId: {
			type: ObjectId,
			index: true,
    },
		imageUrl: String,
		departmentId: {
			type: ObjectId,
			index: true,
		},
		departmentName: String,
    permissionId: {
			type: ObjectId,
			index: true,
		},
		permissionName: String,
		doorId: {
			type: ObjectId,
			index: true,
		},
		doorName: String,
		timestamp: Number
	}, {
    timestamps: true,
  }
)

module.exports = mongoose.model(modelType.logType, logSchema);
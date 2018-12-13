const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const permissionSchema = Schema(
  {
    name: String,
    level: Number,
  }
)

module.exports = mongoose.model(modelType.permissionType, permissionSchema);
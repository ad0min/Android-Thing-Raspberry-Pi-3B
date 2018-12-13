const mongoose = require('mongoose');
const { Schema, SchemaTypes } = mongoose;
const modelType = require('./type');
const { String, Number, ObjectId } = SchemaTypes;

const departmentSchema = Schema(
  {
    name: String,
  }
)

module.exports = mongoose.model(modelType.departmentType, departmentSchema);
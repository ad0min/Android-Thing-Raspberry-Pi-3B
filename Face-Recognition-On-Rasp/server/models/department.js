import mongoose, { Schema, SchemaType } from mongoose;
import modelType from './type';
const { String, Number, ObjectId } = SchemaTypes;

const departmentSchema = Schema(
  {
    name: String,
  }
)

export default mongoose.model(modelType.departmentType, departmentSchema);
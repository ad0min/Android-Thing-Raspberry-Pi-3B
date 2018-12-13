import mongoose, { Schema, SchemaType } from mongoose;
import modelType from './type';
const { String, Number, ObjectId } = SchemaTypes;

const permissionSchema = Schema(
  {
    name: String,
    level: Number,
  }
)

export default mongoose.model(modelType.permissionType, permissionSchema);
import mongoose, { Schema, SchemaType } from mongoose;
import modelType from './type';
const { String, Number, ObjectId } = SchemaTypes;

const doorSchema = Schema(
  {
		name: String, 
		avatar: String,
		departmentId: {
			type: ObjectId,
			index: true,
		},
		minLevelPermission: Number,
	}
)

export default mongoose.model(modelType.doorType, doorSchema);
import mongoose, { Schema, SchemaType } from mongoose;
import modelType from './type';

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

export default mongoose.model(modelType.userType, userSchema);
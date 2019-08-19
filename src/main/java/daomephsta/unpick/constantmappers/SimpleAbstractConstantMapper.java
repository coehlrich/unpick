package daomephsta.unpick.constantmappers;

import java.util.Map;
import java.util.logging.Level;

import daomephsta.unpick.constantmappers.datadriven.parser.UnpickSyntaxException;
import daomephsta.unpick.representations.*;
import daomephsta.unpick.representations.ReplacementInstructionGenerator.Context;

public abstract class SimpleAbstractConstantMapper implements IConstantMapper
{
	protected final Map<String, ReplacementInstructionGenerator> constantGroups;
	
	protected SimpleAbstractConstantMapper(Map<String, ReplacementInstructionGenerator> constantGroups)
	{
		this.constantGroups = constantGroups;
	}
	
	protected abstract TargetMethodIndex getTargetMethodIndex();

	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor)
	{
		return getTargetMethodIndex().targets(methodOwner, methodName, methodDescriptor);
	}
	
	@Override
	public boolean targets(String methodOwner, String methodName, String methodDescriptor, int parameterIndex)
	{
		return getTargetMethodIndex().targets(methodOwner, methodName, methodDescriptor, parameterIndex);
	}
	
	@Override
	public void map(String methodOwner, String methodName, String methodDescriptor, int parameterIndex, Context context)
	{	
		String constantGroupID = getTargetMethodIndex().getParameterConstantGroup(methodOwner, methodName, methodDescriptor, parameterIndex);
		ReplacementInstructionGenerator constantGroup = constantGroups.get(constantGroupID);
		if (constantGroup == null)
		{
			throw new UnpickSyntaxException(String.format("The constant group '%s' does not exist. Target Method: %s.%s%s Parameter Index: %d",
				constantGroupID, methodOwner, methodName, methodDescriptor, parameterIndex));
		}
		if (!constantGroup.canReplace(context))
		{
			context.getLogger().log(Level.INFO, "Transformation skipped. Constant group '%s' cannot transform this invocation.", constantGroupID);
			return;
		}
		
		constantGroup.generateReplacements(context);
		context.getLogger().log(Level.INFO, "Transformation complete");
	}
}

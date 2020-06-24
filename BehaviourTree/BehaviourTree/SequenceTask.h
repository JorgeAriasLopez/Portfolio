#pragma once
#include "ParentTask.h"
class SequenceTask :
	public ParentTask
{
public:

	SequenceTask();
	SequenceTask(Blackboard* bb);

	virtual void ChildSucceded() override;
	virtual void ChildFailure() override;
	virtual void SelectNext() override;
};


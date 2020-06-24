#pragma once
#include "ParentTask.h"
class SelectorTask :
	public ParentTask
{
public:
	SelectorTask();
	SelectorTask(Blackboard* bb);

	// Inherited via ParentTask
	virtual void ChildSucceded() override;
	virtual void ChildFailure() override;
	virtual void SelectNext() override;
};


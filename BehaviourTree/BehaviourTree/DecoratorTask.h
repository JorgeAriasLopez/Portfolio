#pragma once
#include "Task.h"
class DecoratorTask :
	public Task
{
	// Inherited via Task
	virtual bool CheckConditions() override;
	virtual void Execute() override;

	Task* m_ownerTask;
};


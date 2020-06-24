#pragma once
#include "Task.h"

class IdleTask :
	public Task
{
public:
	IdleTask();
	IdleTask(Blackboard* bb);

	void Execute() override;
	bool CheckConditions() override;

	void Start() override;
	void End() override;

};

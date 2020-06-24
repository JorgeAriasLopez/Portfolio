#pragma once
#include "Task.h"

class EatTask :
	public Task
{
public:
	EatTask();
	EatTask(Blackboard* bb);

	void Execute() override;
	bool CheckConditions() override;

	void Start() override;
	void End() override;
};


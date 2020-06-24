#pragma once
#include "Task.h"

class PlayTask :
	public Task
{
public:
	PlayTask();
	PlayTask(Blackboard* bb);

	void Execute() override;
	bool CheckConditions() override;

	void Start() override;
	void End() override;
};


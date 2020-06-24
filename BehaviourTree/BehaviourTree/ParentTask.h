#pragma once
#include "Task.h"
#include <vector>

class ParentTask : public Task 
{

public:
	ParentTask();
	ParentTask(Blackboard* bb);

	~ParentTask();

	void Start() override;

	void AddChild(Task* child);

	Task* GetCurrentTask();
	void NextTask();

	bool CheckConditions() override;
	void Execute() override;

	virtual void ChildSucceded() = 0;
	virtual void ChildFailure() = 0;
	virtual void SelectNext() = 0;

private:
	int m_currentTask;
	std::vector<Task*> m_children;
};


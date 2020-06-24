#pragma once
#include "Blackboard.h"
#include "ParentTask.h"

class BehaviourTree
{
public:
	BehaviourTree();
	virtual ~BehaviourTree();

	int cycles;

	void Execute();
	
	Blackboard* GetBlackboard();
	void SetRoot(ParentTask& root);

private:
	Blackboard* m_blackboard;
	ParentTask* m_rootTask;

};


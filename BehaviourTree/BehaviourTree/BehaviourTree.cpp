#include "BehaviourTree.h"
#include "SequenceTask.h"

BehaviourTree::BehaviourTree()
	: cycles(0), m_rootTask(nullptr)
{
	m_blackboard = new Blackboard();
}

BehaviourTree::~BehaviourTree()
{
	delete m_blackboard;
}

void BehaviourTree::Execute() {
	if (m_rootTask) {
		if (!m_rootTask->Started()) {
			m_rootTask->Start();
			m_rootTask->Execute();
		}
		else if (m_rootTask->Ended()) {
			m_rootTask->End();
		}
		else {
			m_rootTask->Execute();
		}
	}
}

Blackboard* BehaviourTree::GetBlackboard()
{
	return m_blackboard;
}

void BehaviourTree::SetRoot(ParentTask& root)
{
	m_rootTask = &root;
}



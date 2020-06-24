#include "ParentTask.h"

ParentTask::ParentTask()
	: Task(), m_currentTask(-1)
{
}

ParentTask::ParentTask(Blackboard* bb)
	: Task(bb), m_currentTask(-1)
{
}

ParentTask::~ParentTask()
{
	for (Task* t : m_children) {
		delete t;
	}
	std::vector<Task*>().swap(m_children);
}

void ParentTask::AddChild(Task* child)
{
	m_children.push_back(child);
}

Task* ParentTask::GetCurrentTask()
{
	if (m_currentTask < m_children.size()) {
		return m_children.at(m_currentTask);
	}
	return nullptr;
}

void ParentTask::Start() {
	Task::Start();
	m_currentTask = -1;
	SelectNext();
}
void ParentTask::NextTask()
{
	++m_currentTask;
}

bool ParentTask::CheckConditions()
{
	return (m_children.size() > 0);
}

void ParentTask::Execute()
{
	//Start currentTask
	if (!GetCurrentTask()->Started()) {
		GetCurrentTask()->Start();
	}
	else if(GetCurrentTask()->Ended()){	//If ended
		GetCurrentTask()->End();
		if (GetCurrentTask()->Succeeded()) {
			ChildSucceded();
		}
		else {
			ChildFailure();
		}
		if (Ended()) {	
			End();
			return;
		}
	}
	GetCurrentTask()->Execute();
}

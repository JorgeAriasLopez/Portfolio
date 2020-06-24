#include "Task.h"


Task::Task()
	: m_started(false), m_ended(false), m_success(false), m_blackboard(nullptr){

}

Task::Task(Blackboard* bd)
	: m_started(false), m_ended(false), m_success(false), m_blackboard(bd)
{
}

void Task::Start()
{
	m_started = true;
	m_ended = false;
}

void Task::End()
{
	m_ended = true;
	m_started = false;
}

void Task::FinishWithSuccess()
{
	m_ended = true;
	m_success = true;
}

void Task::FinishWithFailure()
{
	m_ended = true;
	m_success = false;
}

bool Task::Started() const
{
	return m_started;
}

bool Task::Ended() const
{
	return m_ended;
}

bool Task::Succeeded() const
{
	return m_success;
}


Blackboard* Task::GetBlackboard()
{
	return m_blackboard;
}


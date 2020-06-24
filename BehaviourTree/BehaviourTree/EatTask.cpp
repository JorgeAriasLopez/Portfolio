#include "EatTask.h"


EatTask::EatTask()
	: Task() {
}

EatTask::EatTask(Blackboard* bb)
	: Task(bb)
{
}


void EatTask::Execute()
{
	if (CheckConditions()) {
		LOG("EatTask Executed");

		GetBlackboard()->GetValue("energy") += 1;

		FinishWithSuccess();
	}
	else {
		FinishWithFailure();
	}
}

bool EatTask::CheckConditions()
{
	return (GetBlackboard()->GetValue("energy") < 20);
}

void EatTask::Start()
{
	Task::Start();
	LOG("EatStart");
}

void EatTask::End()
{
	Task::End();
	LOG("EatEnd");
}

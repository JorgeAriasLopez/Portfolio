#include "IdleTask.h"

IdleTask::IdleTask()
	: Task()
{
}

IdleTask::IdleTask(Blackboard* bb)
	:Task(bb)
{
}


void IdleTask::Execute()
{
	if(CheckConditions()){
		LOG("IdleTask Executed");

		GetBlackboard()->GetValue("boredom") += 2;
		GetBlackboard()->GetValue("energy") -= 1;

		FinishWithSuccess();
	}
	else {
		FinishWithFailure();
	}
}

bool IdleTask::CheckConditions()
{
	return (GetBlackboard()->GetValue("boredom") < 19 && GetBlackboard()->GetValue("energy") > 0);
}

void IdleTask::Start()
{
	Task::Start();
	LOG("IdleStart");
}

void IdleTask::End()
{
	Task::End();
	LOG("IdleEnd");
}

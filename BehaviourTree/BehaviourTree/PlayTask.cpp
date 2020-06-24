#include "PlayTask.h"

PlayTask::PlayTask()
	: Task()
{
}

PlayTask::PlayTask(Blackboard* bb)
	: Task(bb)
{
}

void PlayTask::Execute()
{
	if(CheckConditions()){
		LOG("PlayTask Executed");

		GetBlackboard()->GetValue("energy") -= 2;
		GetBlackboard()->GetValue("boredom") -= 1;
		FinishWithSuccess();
	}
	else {
		FinishWithFailure();
	}
}

bool PlayTask::CheckConditions()
{
	return (GetBlackboard()->GetValue("energy") > 1 && GetBlackboard()->GetValue("boredom") > 0);
}

void PlayTask::Start()
{
	Task::Start();
	LOG("PlayStart");
}

void PlayTask::End()
{
	Task::End();
	LOG("PlayEnd");
}


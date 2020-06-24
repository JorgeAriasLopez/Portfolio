#include "SelectorTask.h"

void SelectorTask::ChildFailure()
{
	SelectNext();
}

SelectorTask::SelectorTask()
	: ParentTask()
{
}

SelectorTask::SelectorTask(Blackboard* bb)
	: ParentTask(bb)
{
}

void SelectorTask::ChildSucceded()
{
	FinishWithSuccess();
}


void SelectorTask::SelectNext()
{
	NextTask();
	Task* currentTask = GetCurrentTask();
	if (currentTask == nullptr) {
		FinishWithFailure();
	}
	else if (!currentTask->CheckConditions()) {
		SelectNext();	//Recall function -> Choose other task
	}
}

#include "SequenceTask.h"

void SequenceTask::ChildSucceded()
{
	SelectNext();
}

void SequenceTask::ChildFailure()
{
	FinishWithFailure();
}

void SequenceTask::SelectNext()
{
	NextTask();
	Task* currentTask = GetCurrentTask();
	if (currentTask == nullptr) {
		FinishWithSuccess();
	}
	else if (!currentTask->CheckConditions()) {
		FinishWithFailure();
	}
}

#pragma once
#include <iostream>
#include "Blackboard.h"

#define LOG(x) std::cout << x << std::endl
class Task 
{
public:
	//Empty constructor
	Task();
	Task(Blackboard* bd);

	virtual void Start();
	virtual void End();
	virtual bool CheckConditions() = 0;
	virtual void Execute() = 0;

	void FinishWithSuccess();
	void FinishWithFailure();

	bool Started() const;
	bool Ended() const;
	bool Succeeded() const;

	Blackboard* GetBlackboard();
	const Blackboard* GetBlackboard() const;
private:
	bool m_started;
	bool m_ended;
	bool m_success;

	Blackboard* m_blackboard;
};


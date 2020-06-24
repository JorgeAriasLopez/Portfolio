#pragma once
#include <map>
class Blackboard
{
public:
	Blackboard();
	virtual ~Blackboard();

	int& GetValue(const char* key);
	void AddValue(const char* key, const int value);

private:
	std::map<const char*, int> _data;
};


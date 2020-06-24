#include "Blackboard.h"


Blackboard::Blackboard() {

}

Blackboard::~Blackboard() {
	_data.clear();
}

int& Blackboard::GetValue(const char* key)
{	
	return _data.at(key);
}

void Blackboard::AddValue(const char* key, const int value)
{
	_data[key] = value;
}

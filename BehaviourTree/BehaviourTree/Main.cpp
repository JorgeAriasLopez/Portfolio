// BehaviourTree.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
#include <stdlib.h>
#include <time.h>
#include "BehaviourTree.h"
#include "SelectorTask.h"
#include "EatTask.h"
#include "IdleTask.h"
#include "PlayTask.h"


int main()
{
    BehaviourTree bt;

    //Random seed for rand
    srand(time(NULL));
    //Values between 0 and 20  ---> rand() % 21
    bt.GetBlackboard()->AddValue("energy", rand() % 21);
    bt.GetBlackboard()->AddValue("boredom", rand() % 21);

    std::cout << "Energy: ";
    std::cout << bt.GetBlackboard()->GetValue("energy") << std::endl;
    std::cout << "Boredom: ";
    std::cout << bt.GetBlackboard()->GetValue("boredom") << std::endl;

    std::cout << "START BEHAVIOUR TREE EXECUTION" << std::endl;
    SelectorTask root(bt.GetBlackboard());

    root.AddChild(new IdleTask(bt.GetBlackboard()));
    root.AddChild(new PlayTask(bt.GetBlackboard()));
    root.AddChild(new EatTask(bt.GetBlackboard()));

    bt.SetRoot(root);
    while (true) {
        bt.Execute();

        std::cout << "Cycles: " << ++bt.cycles << std::endl;
        std::cout << "Energy: ";
        std::cout << bt.GetBlackboard()->GetValue("energy") << std::endl;
        std::cout << "Boredom: ";
        std::cout << bt.GetBlackboard()->GetValue("boredom") << std::endl;

        std::cin.get();
    }
}


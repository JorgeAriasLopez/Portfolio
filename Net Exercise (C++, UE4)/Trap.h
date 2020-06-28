
//------------ My Class
#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Pawn.h"
#include "Trap.generated.h"


UCLASS()
class CARS_API ATrap : public APawn
{
    GENERATED_BODY()

public:
    // Sets default values for this pawn's properties
    ATrap();

    void SetId(unsigned int id);
    unsigned int GetId() const;

protected:
    //Mesh
    UPROPERTY(EditAnywhere)
        UStaticMeshComponent* m_pMesh;

    UFUNCTION()
        virtual void OnOverlapBegin(AActor* OverlappedActor, AActor* OtherActor) ;

private:
    unsigned int m_fID;

};
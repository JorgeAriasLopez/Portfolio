#include "Trap.h"
#include "Car.h"
#include "Components/BoxComponent.h"
#include "ConstructorHelpers.h"
#include "Components/StaticMeshComponent.h"
#include "GameNet/NetComponent.h"

// Sets default values
ATrap::ATrap()
    : m_fID(0)
{
    PrimaryActorTick.bCanEverTick = false;
    
    UBoxComponent* BoxComponent = CreateDefaultSubobject<UBoxComponent>(TEXT("RootComponent"));
    RootComponent = BoxComponent;
    m_pMesh = CreateDefaultSubobject<UStaticMeshComponent>(TEXT("VisualRepresentation"));
    m_pMesh->SetupAttachment(RootComponent);

    static ConstructorHelpers::FObjectFinder<UStaticMesh> BoxVisualAsset(TEXT("/Engine/BasicShapes/Cube"));
    if (BoxVisualAsset.Succeeded())
    {
        m_pMesh->SetStaticMesh(BoxVisualAsset.Object);
        static ConstructorHelpers::FObjectFinder<UMaterial> TrapMaterial(TEXT("Material'/Game/Textures/Trap'"));
        m_pMesh->SetMaterial(0, (UMaterialInterface*)(TrapMaterial.Object));
    }
    SetActorScale3D(FVector(0.1f, 0.1f, 0.05f));
    AutoPossessAI = EAutoPossessAI::Disabled;

    OnActorBeginOverlap.AddDynamic(this, &ATrap::OnOverlapBegin);
}

void ATrap::OnOverlapBegin(AActor* OverlappedActor, AActor* OtherActor) {
    ACar* car = Cast<ACar>(OtherActor);
    if (car) {
        car->GetNetComponent()->TrapHit(this);
    }
}


void ATrap::SetId(unsigned int id) {
    m_fID = id;
}
unsigned int ATrap::GetId() const {
    return m_fID;
}
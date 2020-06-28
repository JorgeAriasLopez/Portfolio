
#include "Car.h"
#include "Components/StaticMeshComponent.h"
#include "Components/BoxComponent.h"
#include "ConstructorHelpers.h"
#include "Components/InputComponent.h"
#include "CarMovementComponent.h"
#include "GameNet/NetComponent.h"



// Sets default values
ACar::ACar()
    : m_fTrapCooldown(5.0f), m_fRemainingNoInputTime(0.0f), m_bDisabledMovement(false), m_bTrapCooldown(false), m_fTrapCooldownRemaining(0.0f)
{
    // Set this pawn to call Tick() every frame.  You can turn this off to improve performance if you don't need it.
	  PrimaryActorTick.bCanEverTick = true;
      UBoxComponent* BoxComponent = CreateDefaultSubobject<UBoxComponent>(TEXT("RootComponent"));
      RootComponent = BoxComponent;
      m_pMesh = CreateDefaultSubobject<UStaticMeshComponent>(TEXT("VisualRepresentation"));
      m_pMesh->SetupAttachment(RootComponent);
      static ConstructorHelpers::FObjectFinder<UStaticMesh> BoxVisualAsset(TEXT("/Engine/BasicShapes/Cube"));
      if (BoxVisualAsset.Succeeded())
      {
        m_pMesh->SetStaticMesh(BoxVisualAsset.Object);
        static ConstructorHelpers::FObjectFinder<UMaterial> CarMaterial(TEXT("Material'/Game/Textures/Car'"));
        m_pMesh->SetMaterial(0, (UMaterialInterface*)(CarMaterial.Object));
      }
      SetActorScale3D(FVector(0.2f, 0.1f, 0.05f));
      SetActorRotation(FRotator(0.f, 270.f, 0.f));
      AutoPossessAI = EAutoPossessAI::Disabled;
      m_pCarMovement = CreateDefaultSubobject<UCarMovementComponent>(TEXT("CarMovement"));
      m_pNet = CreateDefaultSubobject<UNetComponent>(TEXT("Net"));
}

// Called when the game starts or when spawned
void ACar::BeginPlay()
{
	Super::BeginPlay();
    m_vMovementInput.Set(0.f, 0.f);
}

// Called every frame
void ACar::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);
    //-- My Code
    if (m_bTrapCooldown) {
        m_fTrapCooldownRemaining += DeltaTime;
        if (m_fTrapCooldownRemaining >= m_fTrapCooldown) {
            m_bTrapCooldown = false;
            if (GEngine)
            {
                GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("Can throw trap again"));
            }
        }
    }

    if (m_bDisabledMovement) {
        m_vMovementInput.Set(0.f, 0.f);
        m_fRemainingNoInputTime -= DeltaTime;
        if (m_fRemainingNoInputTime <= 0.0f) {
            m_bDisabledMovement = false;
            if (GEngine)
            {
                GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("RECOVERED INPUT"));
            }
        }

    }
    //--
    m_pNet->SetInput(m_vMovementInput);

}

// Called to bind functionality to input
void ACar::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
  Super::SetupPlayerInputComponent(PlayerInputComponent);
  PlayerInputComponent->BindAxis("Move", this, &ACar::Move);
  PlayerInputComponent->BindAxis("Turn", this, &ACar::Turn);

  //My code ------
  PlayerInputComponent->BindAction("Trap", EInputEvent::IE_Pressed, this, &ACar::ThrowTrap);
}
//Input functions
void ACar::Move(float AxisValue)
{
  m_vMovementInput.Y = FMath::Clamp<float>(AxisValue, -1.0f, 1.0f);
}

void ACar::Turn(float AxisValue)
{
  m_vMovementInput.X = FMath::Clamp<float>(AxisValue, -1.0f, 1.0f);
}

float ACar::GetVelocityMagnitude()
{
  return m_pCarMovement->GetVelocityMagnitude();
}

//My functions --------------

void ACar::ThrowTrap() {
    if (GEngine)
    {
        GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("Throw!"));
    }
    if (CanThrowTrap()) {
        m_bTrapCooldown = true;
        m_fTrapCooldownRemaining = 0.0f;
     
        m_pNet->RequestSpawnTrap();
    }
    else {
        if (GEngine)
        {
            GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, *FString("On cooldown!"));
        }
    }
}

void ACar::DisableCarInput(float _time) {
    if (GEngine)
    {
        GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, *FString("Disable car input"));
    }
    m_fRemainingNoInputTime = _time;
    m_bDisabledMovement = true;
}

bool ACar::CanThrowTrap() const
{
    return !m_bTrapCooldown;
}



// Fill out your copyright notice in the Description page of Project Settings.


#include "NetComponent.h"
#include "DrawDebugHelpers.h"
#include "CarsGameInstance.h"
#include "Game/Car.h"
#include "DrawDebugHelpers.h"
#include "Engine/World.h"
#include "GameNet/GameBuffer.h"
#include "Net/Manager.h"
#include "Game/Trap.h"
#include "Game/CarMovementComponent.h"

namespace
{
	const float s_fSnapshotDelay = 1.f;
	const float s_fMaxError = 100.f;
}

// Sets default values for this component's properties
UNetComponent::UNetComponent()
{
	// Set this component to be initialized when the game starts, and to be ticked every frame.  You can turn these features
	// off to improve performance if you don't need them.
	PrimaryComponentTick.bCanEverTick = true;

	// ...
}


// Called when the game starts
void UNetComponent::BeginPlay()
{
	Super::BeginPlay();

	m_pManager = Net::CManager::getSingletonPtr();
	
}


void UNetComponent::SetInput(FVector2D _vInput)
{
	if (m_vMovementInput != _vInput)
	{
		m_vMovementInput = _vInput;
		m_bSendCommand = true;
	}
}


// Called every frame
void UNetComponent::TickComponent(float DeltaTime, ELevelTick TickType, FActorComponentTickFunction* ThisTickFunction)
{
	Super::TickComponent(DeltaTime, TickType, ThisTickFunction);

	UCarsGameInstance* pGI = Cast<UCarsGameInstance>(GetWorld()->GetGameInstance());

	ACar* pCar = pGI->GetGameNetMgr()->GetOwnCar();

	bool bOwner = pCar == GetOwner();
	if (m_pManager->getID() == Net::ID::SERVER || bOwner)
	{
		m_fTimeToNextSnapshot -= DeltaTime;
		SerializeData();
	}
	if (m_pManager->getID() != Net::ID::SERVER)
	{
		if (bOwner)
		{
			SimulateOwnCarMovement(DeltaTime);
		}
		else
    {
      SimulateCarMovement(DeltaTime);
    }
	}
}

void UNetComponent::SerializeData()
{
	if (m_pManager->getID() == Net::ID::SERVER)
	{
		if (m_fTimeToNextSnapshot <= 0.f)
    {
			m_fTimeToNextSnapshot += s_fSnapshotDelay;
      CGameBuffer oData;
		  Net::NetMessageType xType = Net::ENTITY_MSG;
		  oData.write(xType);
		  UCarsGameInstance* pGameInstance = Cast<UCarsGameInstance>(GetWorld()->GetGameInstance());
		  ACar* pCar = Cast<ACar>(GetOwner());
		  oData.write(pGameInstance->GetGameNetMgr()->GetCarID(pCar));
		  oData.write(GetOwner()->GetActorTransform());
			float fVelocity = pCar->GetCarMovement()->GetVelocityMagnitude();
			oData.write(fVelocity);
      m_pManager->send(oData.getbuffer(), oData.getSize(), true);
		}
	}
	else // client
    {
		if (m_bSendCommand)
		{
			m_bSendCommand = false;
		  CGameBuffer oData;
		  Net::NetMessageType xType = Net::ENTITY_MSG;
		  oData.write(xType);
		  oData.write(m_pManager->getID());
		  oData.write(m_vMovementInput);
		  m_pManager->send(oData.getbuffer(), oData.getSize(), true);
		}
    }
}


void UNetComponent::DeserializeData(CGameBuffer& _rData)
{
	if (m_pManager->getID() == Net::ID::SERVER)
  {
    FVector2D vInput;
    _rData.read(vInput);
    ACar* pCar = Cast<ACar>(GetOwner());
    pCar->GetCarMovement()->SetInput(vInput);
	}
	else
	{
		FTransform tTrans;
		_rData.read(tTrans);
    float fVelocity;
    _rData.read(fVelocity);
    m_vVelocity = tTrans.GetRotation().GetAxisX() * fVelocity;
		UCarsGameInstance* pGI = Cast<UCarsGameInstance>(GetWorld()->GetGameInstance());
		ACar* pCar = pGI->GetGameNetMgr()->GetOwnCar();
		bool bOwner = pCar == GetOwner();
		if (fVelocity > 0.f && bOwner)
		{
			FRotator oRot = FRotationMatrix::MakeFromX(m_vVelocity).Rotator();
			GetOwner()->SetActorRotation(oRot);
		}
		FVector vDesiredPos = tTrans.GetLocation();
		FVector vCurrenPos = GetOwner()->GetActorLocation();
		m_vError = vDesiredPos - vCurrenPos;
		if (m_vError.SizeSquared() < s_fMaxError * s_fMaxError)
		{
			FVector vTargetPos = vDesiredPos + m_vVelocity * s_fSnapshotDelay;
			m_vVelocity = (vTargetPos - vCurrenPos) / s_fSnapshotDelay;
		}
		else
		{
			GetOwner()->SetActorTransform(tTrans);
		}
	}
}


void UNetComponent::SimulateCarMovement(float DeltaTime)
{
  ACar* pCar = Cast<ACar>(GetOwner());
  pCar->GetCarMovement()->MoveActor(m_vVelocity, DeltaTime);
}

void UNetComponent::SimulateOwnCarMovement(float DeltaTime)
{
  ACar* pCar = Cast<ACar>(GetOwner());
	pCar->GetCarMovement()->SetInput(m_vMovementInput);
	FTransform oTrans = GetOwner()->GetTransform();
	FVector vReduction = m_vError * DeltaTime;
	if (vReduction.SizeSquared() > m_vError.SizeSquared())
	{
		vReduction = m_vError;
	}
	m_vError -= vReduction;
	oTrans.AddToTranslation(vReduction);
	GetOwner()->SetActorTransform(oTrans);
}


//--------MY FUNCTION
void UNetComponent::RequestSpawnTrap() const{
	if (m_pManager->getID() != Net::ID::SERVER) {
		if (GEngine)
		{
			GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, *FString("Send SpawnTrap!"));
		}
		CGameBuffer oData;
		Net::NetMessageType xType = Net::REQUEST_SPAWN_TRAP;
		oData.write(xType);
		oData.write(m_pManager->getID());	//Cars id
		m_pManager->send(oData.getbuffer(), oData.getSize(), true);
	}
}

void UNetComponent::TrapHit(ATrap* _trap) const {
	//Server got an onverlap and will broadcast what happend
	if (m_pManager->getID() == Net::ID::SERVER) {
		if (GEngine)
		{
			GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, *FString("Notify trap hit!"));
		}

		CGameBuffer oData;
		Net::NetMessageType xType = Net::DESPAWN_TRAP;
		oData.write(xType);
		oData.write(_trap->GetId());	//Traps id
		
		UCarsGameInstance* pGI = Cast<UCarsGameInstance>(GetWorld()->GetGameInstance());
		oData.write(pGI->GetGameNetMgr()->GetCarID(Cast<ACar>(GetOwner())));	//Car owners id

		m_pManager->send(oData.getbuffer(), oData.getSize(), true);
		_trap->Destroy(); //Destroy trap on server (dont need to disable input)
	}
}
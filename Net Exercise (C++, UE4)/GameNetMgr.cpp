// Fill out your copyright notice in the Description page of Project Settings.

#include "GameNetMgr.h"
#include "GameNet/GameBuffer.h"
#include "Net/Paquete.h"
#include "CarsGameInstance.h"
#include "Kismet/GameplayStatics.h"
#include "Game/Car.h"
#include "Game/Trap.h"
#include "GameNet/NetComponent.h"
#include "Engine/World.h"
#include "Engine/LevelStreaming.h"

CGameNetMgr::CGameNetMgr()
{
  Net::CManager::Init();
  m_pManager = Net::CManager::getSingletonPtr();
  m_pManager->addObserver(this);
}

CGameNetMgr::CGameNetMgr(UCarsGameInstance* _pOwner)
  : m_pCarsGameInstance(_pOwner)
{
  Net::CManager::Init();
  m_pManager = Net::CManager::getSingletonPtr();
  m_pManager->addObserver(this);
}

CGameNetMgr::~CGameNetMgr()
{
  m_pManager->removeObserver(this);
  Net::CManager::Release();
}

void CGameNetMgr::dataPacketReceived(Net::CPaquete* packet)
{
  CGameBuffer data;
  data.write(packet->getData(), packet->getDataLength());
  data.reset();
  Net::NetMessageType xType;
  data.read(xType);
  switch (xType)
  {
  case Net::LOAD_MAP:
  {
    char sLevel[64];
    data.read(sLevel);
    UGameplayStatics::OpenLevel(m_pCarsGameInstance->GetWorld(), sLevel);
    CGameBuffer data2;
    Net::NetMessageType iID2 = Net::NetMessageType::MAP_LOADED;
    data2.write(iID2);
    m_pManager->send(data2.getbuffer(), data2.getSize(), true);
  } break;
  case Net::MAP_LOADED:
  {
    ++m_uMapLoadedNotifications;
    if (m_uMapLoadedNotifications >= m_pManager->getConnections().size())
    {
      for (auto client : m_pManager->getConnections())
      {
        CGameBuffer dataCar;
        Net::NetMessageType iIDLoad = Net::NetMessageType::LOAD_PLAYER;
        dataCar.write(iIDLoad);
        dataCar.write(client.first);
        FVector vPos(220.f, -310.f + client.first * 40.f, 0.f);
        dataCar.write(vPos);
        m_pManager->send(dataCar.getbuffer(), dataCar.getSize(), true);
        CreateCar(client.first, vPos);
      }
    }
  } break;
  case Net::LOAD_PLAYER:
  {
    unsigned int uClient;
    data.read(uClient);
    FVector vPos;
    data.read(vPos);
    CreateCar(uClient, vPos);
    if (uClient == m_pManager->getID())
    {
      APlayerController* pPC = GEngine->GetFirstLocalPlayerController(m_pCarsGameInstance->GetWorld());
      if (pPC)
      {
        pPC->Possess(m_vPlayers[uClient]);
      }
    }
  } break;
  case Net::ENTITY_MSG:
  {
      unsigned int uClient;
      data.read(uClient);
      ACar* pCar = m_vPlayers.at(uClient);
      pCar->GetNetComponent()->DeserializeData(data);
  }break;

  //--MY CODE
  case Net::REQUEST_SPAWN_TRAP:
  { 
      if (m_pManager->getID() == Net::ID::SERVER) { 
        unsigned int uClient;
        data.read(uClient);
        ACar* pCar = m_vPlayers.at(uClient);
        
        if (pCar->CanThrowTrap()) {
            if (GEngine)
            {
                GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("Got spawn as server!"));
            }
            CGameBuffer oData;
            xType = Net::SPAWN_TRAP;
            oData.write(xType);
            FVector vPosition = pCar->GetActorLocation() + pCar->GetActorForwardVector() * -15.0f;
            oData.write(vPosition);
            m_pManager->send(oData.getbuffer(), oData.getSize(), true);

            SpawnTrap(vPosition);   //Spawn on server side
            pCar->ThrowTrap();  //Start car's trap cooldown. Need to have a register on server to avoid cheating
        }
        else {
            if (GEngine)
            {
                GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Purple, FString::Printf(TEXT("Maybe %d is cheating"), uClient));
            }
        }
    }

  }break;
  //-- MY CODE
  case Net::SPAWN_TRAP:
  {
      if (GEngine)
      {
          GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("SpawnTrap!"));
      }
      FVector vPosition;
      data.read(vPosition);
      SpawnTrap(vPosition);
  }break;
  case Net::DESPAWN_TRAP:
  {
      if (GEngine)
      {
          GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Green, *FString("Got despawn trap!"));
      }
      unsigned int uId;
      data.read(uId);   //Trap id
      DespawnTrap(uId); //All must despawn the trap
      data.read(uId);   //Car id
      if (m_pManager->getID() == uId) {
          GetOwnCar()->DisableCarInput(1.5f); //Only need to disable car input in one client
      }
  }
  //-------
  default: break;
  }
}

void CGameNetMgr::connexionPacketReceived(Net::CPaquete* packet)
{
  if (m_pManager->getID() == Net::ID::SERVER)
  {
    if (GEngine)
    {
      GEngine->AddOnScreenDebugMessage(-1, 5.0f, FColor::Red, "Client Connected!");
    }
  }
}

void CGameNetMgr::disconnexionPacketReceived(Net::CPaquete* packet)
{

}

void CGameNetMgr::CreateCar(int _iClient, FVector _vPos)
{
  FActorSpawnParameters SpawnInfo;
  SpawnInfo.Name = FName("Car", _iClient);
  SpawnInfo.SpawnCollisionHandlingOverride = ESpawnActorCollisionHandlingMethod::AlwaysSpawn;
  ACar* pCar = m_pCarsGameInstance->GetWorld()->SpawnActor<ACar>(_vPos, FRotator::ZeroRotator, SpawnInfo);
  if (pCar)
  {
    m_vPlayers[_iClient] = pCar;
    m_vPlayerIDs[pCar] = _iClient;
  }
}


ACar* CGameNetMgr::GetOwnCar() const
{
  if (m_pManager->getID() != Net::ID::SERVER)
  {
    return m_vPlayers.at(m_pManager->getID());
  }
  return nullptr;
}

unsigned int CGameNetMgr::GetCarID(ACar* _pCar) const
{
  return m_vPlayerIDs.at(_pCar);
}

//------ MY CODE
void CGameNetMgr::SpawnTrap(FVector _vPos)
{
    ++m_uNextTrapID;
    FActorSpawnParameters SpawnInfo;
    SpawnInfo.Name = FName("Trap", m_uNextTrapID);
    SpawnInfo.SpawnCollisionHandlingOverride = ESpawnActorCollisionHandlingMethod::AlwaysSpawn;
    ATrap* pTrap = m_pCarsGameInstance->GetWorld()->SpawnActor<ATrap>(_vPos, FRotator::ZeroRotator, SpawnInfo);
    if (pTrap)
    {
        pTrap->SetId(m_uNextTrapID);
        m_vTraps[m_uNextTrapID] = pTrap;
    }
}

void CGameNetMgr::DespawnTrap(unsigned int _id)
{
    ATrap* trap = m_vTraps.at(_id);
    if (trap) {
        trap->Destroy();
        m_vTraps.erase(_id);
    }
   
}

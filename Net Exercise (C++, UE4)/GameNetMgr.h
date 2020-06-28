
#pragma once

#include "Net/Manager.h"
#include "Math/Vector.h"

class UCarsGameInstance;
class ACar;
class ATrap;

class CGameNetMgr : public Net::CManager::IObserver
{
public:
  CGameNetMgr();
  CGameNetMgr(UCarsGameInstance* _pOwner);
  virtual ~CGameNetMgr();

  ACar* GetOwnCar() const;
  unsigned int GetCarID(ACar* _pCar) const;

  virtual void dataPacketReceived(Net::CPaquete* packet);
  virtual void connexionPacketReceived(Net::CPaquete* packet);
  virtual void disconnexionPacketReceived(Net::CPaquete* packet);

private:
  void CreateCar(int _iClient, FVector _vPos);

private:
  UCarsGameInstance* m_pCarsGameInstance = nullptr;
  Net::CManager* m_pManager = nullptr;
  unsigned int m_uMapLoadedNotifications = 0u;
  std::map<unsigned int, ACar*> m_vPlayers;
  std::map<ACar*, unsigned int> m_vPlayerIDs;

  //---MY CODE
  void SpawnTrap(FVector _vPos);
  void DespawnTrap(unsigned int id);
  std::map<unsigned int, ATrap*> m_vTraps;
  unsigned int m_uNextTrapID = 0u;
};

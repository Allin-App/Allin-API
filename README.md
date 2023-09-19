<div align = center>

  <img src="Documentation/Images/Banner-AllIn.png" />
    
---

&nbsp; ![Docker](https://img.shields.io/badge/Docker-2496ED.svg?style=for-the-badge&logo=Docker&logoColor=white)
&nbsp; ![CSharp](https://img.shields.io/badge/C%20Sharp-239120.svg?style=for-the-badge&logo=C-Sharp&logoColor=white)
&nbsp; ![Visual Studio](https://img.shields.io/badge/Visual%20Studio-5C2D91.svg?style=for-the-badge&logo=Visual-Studio&logoColor=white)
&nbsp; ![Swagger](https://img.shields.io/badge/Swagger-85EA2D.svg?style=for-the-badge&logo=Swagger&logoColor=black)
&nbsp; ![.NET](https://img.shields.io/badge/.NET-512BD4.svg?style=for-the-badge&logo=dotnet&logoColor=white)

### API - ALL IN !

**Contexte** : Api pour le projet universitaire de troisieme année (B.U.T Informatique de Clermont-Ferrand) nommé "All In" 

# Répartition du gitlab

[**Sources**](Sources) : **Code de l'application**

[**Documentation**](Documentation) : **Documentation de l'application**

👉 [**Solution de l'application**](Sources/AllIn.sln)

## Environnement de Travail

Pour l'API :👇

<div align = center>

---

&nbsp; ![CSharp](https://img.shields.io/badge/C%20Sharp-239120.svg?style=for-the-badge&logo=C-Sharp&logoColor=white)
&nbsp; ![Visual Studio](https://img.shields.io/badge/Visual%20Studio-5C2D91.svg?style=for-the-badge&logo=Visual-Studio&logoColor=white)
&nbsp; ![Swagger](https://img.shields.io/badge/Swagger-85EA2D.svg?style=for-the-badge&logo=Swagger&logoColor=black)
&nbsp; ![.NET](https://img.shields.io/badge/.NET-512BD4.svg?style=for-the-badge&logo=dotnet&logoColor=white)
---

</div>
## Diagramme de classes du modèle

```mermaid
classDiagram
direction LR;

class LargeImage{
    +/Base64 : string
}

class User{
    +/ Id : string
    +/ Pseudo : string
    +/ Mail : string
    +/ Password : string
    + CreationDate : DateTime
    + AllCoins : int
    ~ AddGroup(group : Group) bool
    ~ RemoveGroup(group : Group) bool
}
User --> "1" LargeImage : Image
Group --> "1" LargeImage : Image

class Bet{
    +/ Id : string    
    +/ Title : string
    +/ Name : string
    +/ Choices : List<string>
    +/ Theme: string
    +/ Status: bool
    + Description : string
    + StartDate : DateTime
    + EndDate : DateTime
}
Bet --> "*" User : Dictionary~User,Mise~

class Mise{
    +/ Cost : int    
    +/ Choice : string
}

class Group{
    +/ Id : string
    +/ Name : string    
    +/ Image : string
    +/ CreationDate : DateTime
}
User --> "*" Group : groups
```

## Diagramme de classes du modèle
```mermaid
classDiagram
direction LR;
class IGenericDataManager~T~{
    <<interface>>
    GetNbItems() Task~int~
    GetItems(index : int, count : int, orderingPropertyName : string?, descending : bool) Task~IEnumerable~T~~
    GetNbItemsByName(substring : string)
    GetItemsByName(substring : string, index : int, count : int, orderingPropertyName : string?, descending : bool) Task~IEnumerable~T~~
    UpdateItem(oldItem : T, newItem : T) Task~T~~
    AddItem(item : T) Task~T~
    DeleteItem(item : T) Task~bool~
}
class IUsersManager{
    <<interface>>
    GetNbItemsByName(name : string)
    GetItemsByName(name : string, index : int, count : int, orderingPropertyName : string?, descending : bool) Task~IEnumerable~Bet?~~
    GetItemByMail(mail : string)
}
class IBetsManager{
    <<interface>>
    GetNbItemsByUser(user : User?)
    GetItemsByUser(user : User?, index : int, count : int, orderingPropertyName : string?, descending : bool) Task~IEnumerable~Bet?~~
}

class IGroupsManager{
    <<interface>>
    GetNbItemsByName(name : string)
    GetItemsByName(name : string, index : int, count : int, orderingPropertyName : string?, descending : bool) Task~IEnumerable~Group?~~
}

IGenericDataManager~User?~ <|.. IUsersManager : T--User?
IGenericDataManager~Bet?~ <|.. IBetsManager : T--Bet?
IGenericDataManager~Group?~ <|.. IGroupsManager : T--Group?
class IDataManager{
    <<interface>>
}
IUsersManager <-- IDataManager : UsersMgr
IBetsManager <-- IDataManager : BetsMgr
IGroupsManager <-- IDataManager : GroupsMgr
```

## Diagramme de classes simplifié du Stub
```mermaid
classDiagram
direction TB;

IDataManager <|.. StubData

UsersManager ..|> IUsersManager
StubData --> UsersManager

BetsManager ..|> IBetsManager
StubData --> BetsManager

GroupsManager ..|> IGroupsManager
StubData --> GroupsManager

StubData --> "*" User
StubData --> "*" Bet
StubData --> "*" Group
```
## Deploiement
- [x] &nbsp; ![IOS](https://img.shields.io/badge/IOS-000?style=for-the-badge&logo=apple&logoColor=black&color=white)
- [x] &nbsp; ![Android](https://img.shields.io/badge/Android-000?style=for-the-badge&logo=android&logoColor=white&color=green)

## Technicien en charge de l'API

- HASSOU Rayhan
- EVARD Lucas 
<div align = right>
<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Licence Creative Commons" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a>
<right>

</div>
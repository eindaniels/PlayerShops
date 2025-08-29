# ⚠️ Disclaimer
Dies ist **nicht** das finale Plugin, sondern eine **Entwicklungs- und Testversion**.  
Es kann sich jederzeit ändern, Funktionen können noch unvollständig sein und es gibt keine Garantie für Stabilität oder Kompatibilität.

# 🛒 PlayerShops

> Ein Minecraft-Plugin für **PaperMC/Spigot**, das Spielern ermöglicht, eigene Shops zu erstellen – inspiriert von den **Spielershops** auf [Cytooxien.net](https://cytooxien.net) (Reallife, SkyBlock & CitySMP).

---

## ✨ Features

- ⚡ Spieler können eigene Shops platzieren & verwalten
- 📦 **Stash-System** für Shop-Besitzer (privates Lager für Handelswaren)
- 💰 **Buy/Sell-Toggle** → Besitzer entscheidet, ob andere Spieler kaufen oder verkaufen dürfen
- 📊 **Echtzeit-Label über dem Shop** (zeigt Angebot, Preise & Status)
- 🔒 Automatische Rückgabe von Items bei ungültigen Einlagerungen
- 💸 Vault-Integration für sicheres Bezahlen
- 🖼️ Verwendet die **Minecraft Display-Entities**:
    - `ItemDisplay` → zeigt das gehandelte Item schwebend über dem Shop
    - `TextDisplay` → zeigt Titel, Preis & Status in einem schönen schwebenden Text
    - `Interaction` → sorgt für Klick-Hitbox, um Shops zu öffnen

---

## 📋 Anforderungen

- **Minecraft:** 1.21+
- **Server:** [PaperMC](https://papermc.io) (empfohlen) oder Spigot
- **Vault:** benötigt (für Economy)
- **Economy Plugin:** z. B. EssentialsX, CMI oder ein anderes Vault-kompatibles Plugin

---

## 🚀 Installation

1. Lade die neueste Version von **PlayerShops** herunter.
2. Lege die `.jar`-Datei in den `plugins/`-Ordner deines Servers.
3. Stelle sicher, dass **Vault** und ein Economy-Plugin installiert sind.
4. Starte den Server neu.
5. Erstelle deinen ersten Shop & genieße den Handel!
---

## ⚙️ Commands

### 🏗️ Spieler Commands

| Befehl                                       | Beschreibung                                                      |
|----------------------------------------------|-------------------------------------------------------------------|
| `/createshop <buyPrice> <sellPrice> <menge>` | Erstellt einen neuen Shop über dem Block, den du ansiehst.        |
| `/removeshop`                                | Entfernt den Shop über dem Block, den du ansiehst (nur Besitzer). |

👉 Shops werden durch **Rechtsklick auf die Interaktion-Hitbox** geöffnet:
- **Besitzer:** Stash-GUI (Einlagern/Verwalten)
- **Andere Spieler:** Handels-GUI (Kaufen/Verkaufen)

---

## 🔑 Permissions

| Permission          | Beschreibung                                              |
|---------------------|-----------------------------------------------------------|
| `playershop.create` | Erlaubt das Erstellen eines Shops mit `/createshop`       |
| `playershop.remove` | Erlaubt das Entfernen des eigenen Shops mit `/removeshop` | 

---

## 📦 Konfiguration

- Preise & Item-Menge pro Trade können pro Shop eingestellt werden.
- Shop-Besitzer können im **Stash-GUI**:
    - Items einlagern / entnehmen
    - Kaufen/Verkaufen toggeln
    - Preise anpassen

---

## 🏆 Inspiration

Dieses Plugin ist **inspiriert** von den **Spielershops** auf **Cytooxien.net**.  
Besonders die Systeme aus:
- 🏙️ **Reallife**
- 🌌 **SkyBlock**
- 🌆 **CitySMP**

Die Idee stammt von dort – dieses Plugin ist eine **eigene Umsetzung** und keine offizielle Ressource von Cytooxien.

---

## 📦 Version & Infos

- **Plugin-Version:** 1.0.0 (Beta)
- **Autor:** Eindaniel
- **API:** PaperMC 1.20+
- **Lizenz:** MIT (frei verwendbar & anpassbar, Credits erwünscht)
- 
## 💡 Idee / Mitmachen

- Ideen für neue Features?
- Fehler gefunden?
- Pull Requests & Issues sind willkommen!

---

## ❤️ Credits

- Idee & Inspiration: **Cytooxien.net** (Spielershops)
- Umsetzung: **eindaniels**
- Economy-Schnittstelle: [Vault](https://www.spigotmc.org/resources/vault.34315/)

---

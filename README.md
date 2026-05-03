# ⚠️ Disclaimer
This is **not the final plugin**, but a **development and test version**.  
Things may change at any time, features may be incomplete, and stability is not guaranteed.

# 🛒 PlayerShops

> A Minecraft plugin for **PaperMC/Spigot** that allows players to create their own shops – inspired by the **player shops** on Cytooxien.net (SkyBlock).

---

## ✨ Features

- ⚡ Players can create and manage their own shops
- 📦 **Stash system** for shop owners (private storage for trade items)
- 💰 **Buy/Sell toggle** → owners can decide whether players can buy or sell
- 📊 **Real-time label above the shop** (shows item, price & status)
- 🔒 Automatic return of invalid items placed into the stash
- 💸 Vault integration for secure payments
- 🖼️ Uses modern **Minecraft Display Entities**:
  - `ItemDisplay` → shows the item floating above the shop
  - `TextDisplay` → displays name, price & status as floating text
  - `Interaction` → handles click hitbox for opening the shop

---

## 📋 Requirements

- **Minecraft:** 1.21+
- **Server:** PaperMC (recommended) or Spigot
- **Vault:** required (for economy)
- **Economy Plugin:** e.g. EssentialsX, CMI or any Vault-compatible plugin

---

## 🚀 Installation

1. Download the latest version of **PlayerShops**.
2. Place the `.jar` file into your server's `plugins/` folder.
3. Make sure **Vault** and an economy plugin are installed.
4. Restart the server.
5. Create your first shop & enjoy trading!

---

## ⚙️ Commands

### 🏗️ Player Commands

| Command                                       | Description                                       |
|-----------------------------------------------|---------------------------------------------------|
| `/createshop <buyPrice> <sellPrice> <amount>` | Creates a shop above the block you are looking at |
| `/removeshop`                                 | Removes the shop you are looking at (owner only)  |

👉 Shops are opened by **right-clicking the interaction hitbox**:
- **Owner:** Stash GUI (manage storage)
- **Others:** Trading GUI (buy/sell)

---

## 🔑 Permissions

| Permission          | Description                                      |
|---------------------|--------------------------------------------------|
| `playershop.create` | Allows creating shops with `/createshop`         |
| `playershop.remove` | Allows removing your own shop with `/removeshop` |

---

## 📦 Configuration

- Prices & item amounts can be configured per shop.
- In the **Stash GUI**, owners can:
  - Store / withdraw items
  - Toggle buying and selling
  - Change prices

---

## 🏆 Inspiration

This plugin is **inspired** by the player shop system on **Cytooxien.net**.  
Especially from:
- 🌌 SkyBlock

This is an independent implementation and not an official Cytooxien resource.

---

## 📦 Version & Info

- **Plugin Version:** 1.0.0 (Beta)
- **Author:** eindaniel
- **API:** PaperMC 1.20+
- **License:** MIT (free to use & modify, credits appreciated)

---

## 💡 Contributing

- Got ideas for new features?
- Found a bug?

Pull requests & issues are welcome!

---

## ❤️ Credits

- Idea & inspiration: Cytooxien.net
- Development: eindaniel
- Economy API: Vault
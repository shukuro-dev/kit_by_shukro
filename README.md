# kit_by_shukro

Custom PvP Kit Builder and Supply Menu plugin for **Paper 1.21.4** (Java 21) under group `shkr`.

## Features
- `/kits` opens an infinite supply menu with PvP gear, armor, crystals, obsidian, and golden apples.
- `/kit create <1-4>` saves your full inventory and armor into slot `1`, `2`, `3`, or `4` (`kits.yml`).
- `/kit <1-4>` equips your saved armor and gives your saved inventory items.
- Custom tab completion without player names.
- `/kit reload` reloads `config.yml` and `kits.yml`.

## Commands
- `/kits` — Open the infinite PvP supply menu.
- `/kit create <1|2|3|4>` — Save your current inventory and armor to a kit slot.
- `/kit <1|2|3|4>` — Claim a saved kit slot.
- `/kit reload` — Reload configuration and saved kits (`kitbyshukro.admin` or OP).

## Build
```bash
mvn clean package
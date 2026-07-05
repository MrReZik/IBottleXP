# IBottleXP
[![SpigotMC Downloads](https://img.shields.io/spiget/downloads/000000?label=SpigotMC%20Downloads&logo=spigotmc&color=orange)](https://www.spigotmc.org/)
[![SpigotMC Version](https://img.shields.io/spiget/version/000000?label=SpigotMC%20Version&logo=spigotmc)](https://www.spigotmc.org/)
[![Tested Versions](https://img.shields.io/badge/Tested%20Versions-1.21.4-brightgreen)](https://www.spigotmc.org/)
[![MIT License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

A lightweight and fully configurable experience bottle plugin with hex color support, custom sounds, cooldowns, particles, and per-player statistics.

**Links:**
- [SpigotMC (Download)](https://www.spigotmc.org/)
- [Wiki (Documentation)](../../wiki)
- [Discord (Support)](https://discord.gg/)

---

## Support
The best way to get support is by joining our **Discord Server**. You can also open an issue here on GitHub if you found a bug or want to request a feature.

---

## Minecraft Limitations
- Custom item names use Spigot's legacy color API — gradient support requires Paper.
- Bottles are identified via PersistentDataContainer, so renaming them on an anvil will not break their functionality.
- Particle effects are server-side and visible only to the player using the bottle.
- Sounds depend on the values available in `org.bukkit.Sound` for your server version.

---

## Contributing
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)

Pull requests are welcome. For major changes please open an issue first to discuss what you would like to change.

---

## Building
Building IBottleXP is straightforward. You need **JDK 21**, **Maven 3.6+**, and **Git**.

1. Clone the repository to your machine.
2. Open a terminal at the project root.
3. Run `mvn clean package` and IBottleXP will build.
4. You can find the jar at `./target/IBottleXP-1.0.0.jar`.

On **Windows** you can also just run the included `build.bat` — it handles everything automatically and optionally copies the jar to your plugins folder.

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/ibottlexp help` | Show all commands | — |
| `/ibottlexp reload` | Reload config and messages | `ibottlexp.reload` |
| `/ibottlexp give <player> <bottle> [amount]` | Give a bottle to a player | `ibottlexp.give` |
| `/ibottlexp list` | List all configured bottles | — |
| `/ibottlexp info <bottle>` | Show info about a bottle | — |
| `/ibottlexp stats [player]` | Show XP statistics | — / `ibottlexp.admin` |

**Aliases:** `/ixp`, `/bottlexp`

---

## Permissions

| Permission | Default | Description |
|---|---|---|
| `ibottlexp.use` | everyone | Use XP bottles |
| `ibottlexp.reload` | op | Reload the plugin |
| `ibottlexp.give` | op | Give bottles to players |
| `ibottlexp.admin` | op | Full access including other players' stats |

---

## Configuration

**config.yml** — define your bottles:

```yaml
bottles:

  bottle1:
    name: "&#00FF00Top Bottle"
    levels: 50
    lore:
      - "&#aaaaaa──────────────────"
      - "&#FFFFFFGives &a50 &flevels of XP"
      - "&#aaaaaa──────────────────"
    sound: ENTITY_EXPERIENCE_ORB_PICKUP
    sound-volume: 1.0
    sound-pitch: 1.0
    cooldown: 5
    action-bar: "&#00FF00+50 levels!"
```

**messages.yml** — edit every plugin message:

```yaml
prefix: "&#00FFFFIBottleXP &8» &f"
bottle-used: "%prefix%You used %bottle% and got &#00FF00+%levels% &flevels!"
cooldown-active: "%prefix%&#FF6600Wait &#FFAA00%time%s &fbefore using %bottle% again!"
reload-success: "%prefix%&#00FF00Config reloaded successfully!"
```

Both files support `&#rrggbb` hex colors and standard `&` color codes everywhere.

---

## bStats
This plugin uses **bStats** to collect anonymous usage statistics. You can disable this in the bStats config at `plugins/bStats/config.yml`.

# MineboxAddition
MineboxAddition is a Minecraft Fabric Mod for version 1.21.3 and 1.21.4 that introduces exciting features for your Minebox gameplay !

## Features

- Durability indicators for Haversacks and Harvesters
- Shiny mobs alerter, send and receive alerts from other MineboxAdditions users
- Shop opening alerts and their actual selling item (Mouse, Bakery, Buckstar, Cocktail)
- Full moon HUD
- Weather HUD displays (upcoming rain/storm)
- Haversack HUD (fill rate and “full in” estimate)
- Mermaid requests displayed in the HUD
- Item pickup notifications (merging lines, max quantity, configurable duration)
- Item rarity display (modes: fill or circle, adjustable opacity)
- Fishing HUD (configurable display radius)
- In-game HUD editor
- In-game atlas (show recipes, adjustable quantity, lock recipes)
- Performance mode (reduced rendering of certain entities)
- Full-featured voice chat (Private and Proximity) with audio device selection screen (microphone/speakers) + microphone gain adjustment

Each feature can be enabled/disabled. Some are disabled by default, so remember to open the mod settings before playing!

### Keybinds (can be changed in Minecraft keybinds settings)
- O: open mod settings
- L: open audio device screen
- I: open HUD editor
- P: enable/disable performance mode
- Right Shift: open Atlas

### In-game commands
- /mba vc create - create an audio room
- /mba vc join ``<code>`` - join an audio room
- /mba vc proximity - enable/disable proximity audio
- /mba vc leave - leave the audio room
- /mba debug - display diagnostic information about the mod

### Network features informations
When using the mod network features, some informations are sent to the server to keep track of your session and to send you latest features data (eg: Atlas items, Shops Alerts, Shinies...)

The informations used are :
- Your Minecraft username
- Your Minecraft UUID [What's that ?!](https://minecraft.fandom.com/wiki/Universally_unique_identifier)
- Your Minecraft client lang

These informations are NOT stored anywhere, and gets automatically erased from RAM when your session ends (when you disconnect from Minebox)

## Available locales
- FR
- EN
- NL
- PL
- CN

## Requirements
- Minecraft 1.21.3 or 1.21.4
- Fabric Loader 0.16.10 or above
- Fabric API
- Cloth Config API

### Optional
- ModMenu

## Download and Installation
- Download the latest release from [here](https://github.com/Dampen59/MineboxAdditions/releases/latest)
- Drop the downloaded jarfile to your ``\mods`` folder and you're all set !

Be sure you have downloaded the correct version of Fabric Loader and the required dependencies otherwise you may have an error upon starting the game.


## Building from sources

### Clone the repository
``git clone https://github.com/your-username/MineboxAddition.git``

### Build
``./gradlew build``

## Contributing
Contributions are welcome! Please follow these steps:

- Fork the repository
- Create a new branch
- Commit your changes
- Open a pull request targeting ``dev``

## Support

For questions or support, please open an issue on Github or contact me on Discord (dampen59)

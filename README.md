[![][1]][3] [![][2]][3] [![][4]][5] [![][6]][7] [![][8]][9] [![][10]][11]

# Harvest Level Tweaker
A minecraft forge mod that allows modpack makers to add new harvest levels with custom names.

Currently supports:
* [The One Probe](https://www.curseforge.com/minecraft/mc-mods/the-one-probe)
* [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
* [WTHIT](https://www.curseforge.com/minecraft/mc-mods/wthit-forge)
* [Tinker's Construct](https://www.curseforge.com/minecraft/mc-mods/tinkers-construct)

## Table of Contents
<!-- TOC -->
* [Harvest Level Tweaker](#harvest-level-tweaker)
  * [Table of Contents](#table-of-contents)
  * [Add New Harvest Levels](#add-new-harvest-levels)
    * [Example: `cobalt.json`:](#example-cobaltjson)
  * [Level Ordering](#level-ordering)
    * [Example: `ordering.json`:](#example-orderingjson)
  * [Override Item Harvest Level and Harvest Type](#override-item-harvest-level-and-harvest-type)
  * [Set Block Harvest Level](#set-block-harvest-level)
  * [Localization and custom vanilla level names](#localization-and-custom-vanilla-level-names)
  * [Client Config](#client-config)
  * [Commands](#commands)
  * [Set Tinker's Construct Material Level](#set-tinkers-construct-material-level)
<!-- TOC -->

## Add New Harvest Levels
Go to folder `config/hltweaker/levels` <br>
Create a json file with level id as the name. <br> 
For example: `cobalt.json` will create a new level with id `cobalt`<br>
Json Format: <br>
```
{
  "color": "<color>",
  "level": <level>, 
  "icons": {
    "<mineable tag>": "<item id>",
    ...
  }
}
```

| Field   | Type        | Default Value | Description                                                                                               | Example                                                     |
|---------|-------------|---------------|-----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|
| `color` | String      | `"white"`     | Color of the harvest level in hex format (prefix `#`) or color name                                       | `"#0047ab"` or `"blue"`                                     |
| `level` | Integer     | `0`           | Numerical tier of the harvest level for compatibility. <br> Does not affect the actual ordering of levels | `5`                                                         |
| `icons` | Json Object | `{}`          | Icons for the harvest level in `"<mineable_tag>": "<item id>"` format                                     | `{"minecraft:mineable/pickaxe": "minecraft:stone_pickaxe"}` |

### Example: `cobalt.json`:
```json
{
  "color": "#0047ab",
  "icons": {
    "minecraft:mineable/pickaxe": "kubejs:cobalt_pickaxe",
    "minecraft:mineable/axe": "kubejs:cobalt_axe",
    "minecraft:mineable/shovel": "kubejs:cobalt_shovel",
    "minecraft:mineable/hoe": "kubejs:cobalt_hoe"
  }
}
```
This will create a new level with id `cobalt`<br>
It can also be as simple as:
```json
{}
```


## Level Ordering
`config/hltweaker/ordering.json` controls which levels are better or worse than others. <br>
This file will be automatically generated when launching the game if it does not exist. <br>
Levels are ordered from worst to best. <br>
You can also change the order of vanilla levels and levels from other mods, although levels from other mods won't be automatically added to this file. <br>
### Example: `ordering.json`:
```json
[
  "minecraft:wood",
  "minecraft:gold",
  "minecraft:stone",
  "minecraft:iron",
  "minecraft:diamond",
  "hltweaker:cobalt",
  "minecraft:netherite",
  "somemod:somelevel"
]
```


## Override Item Harvest Level and Harvest Type
Find the file `config/hltweaker/item_harvest_level_overrides.json` <br>

Json Format: <br>
```json
{
  "<item id>": {
    "<mineable tag>": "<level id>"
  }
}
```
* `<item id>`: Item id. Example: `minecraft:stone`<br>
* `<mineable tag>`: The mineable tag (required tool type). Example: `minecraft:mineable/pickaxe` <br>
* `<level id>`: Harvest level id (with namespace if it's not from this mod). Example: `cobalt` or `minecraft:wood` <br>



Example `item_harvest_level_overrides.json`: 
```json
{
  "minecraft:golden_pickaxe": {
    "minecraft:mineable/pickaxe": "cobalt",
    "minecraft:mineable/axe": "minecraft:netherite"
  },
  "minecraft:netherite_pickaxe": {
    "minecraft:mineable/pickaxe": "minecraft:wood"
  }
}
```
This will remove all existing harvest types and level of stone pickaxe and netherite pickaxe. <br> 
Then set the harvest level of stone pickaxe to cobalt as a pickaxe and netherite when used as an axe. <br>
And set the harvest level of netherite pickaxe to wood when used as a pickaxe. <br>

## Set Block Harvest Level
Create a datapack and add the blocks to tag: <br>
* Harvest Level Tweaker Levels: `hltweaker:needs_<level id>_tool` <br>
* Vanilla levels: <br>
  * `stone`, `iron`, `diamond`: `minecraft:needs_<level id>_tool` <br>
  * `wood`, `gold`, `netherite`: `forge:needs_<level id>_tool` <br>

Example `data/hltweaker/tags/blocks/needs_cobalt_tool.json`: 
```json
{
  "values": [
    "#minecraft:stone",
    "kubejs:cobalt_block"
  ]
}
```
This will set all blocks under `minecraft:stone` tag and cobalt block to require cobalt level tools. <br>
See more: https://minecraft.wiki/w/Tag

## Localization and custom vanilla level names
* Translation key for custom harvest level is `text.hltweaker.level.<level id>` <br>
Example: `"text.hltweaker.level.cobalt": "Cobalt"` <br>
* Translation key for tool type is `text.hltweaker.tool.<mod namespace>.<tool type>` <br>
Example: `"text.hltweaker.tool.minecraft.pickaxe": "Pickaxe"`, `"text.hltweaker.tool.paxelmod.paxel": "Paxel"` <br>
* Translation key for vanilla harvest level is `text.hltweaker.level.minecraft.<wood/gold/stone/iron/diamond/netherite>` <br>
Example: `"text.hltweaker.level.minecraft.wood": "This is Wood Level"` <br>

## Client Config
Client config is located at `config/hltweaker/client.toml` <br>
You can change the color of vanilla harvest levels in the config.<br>
More details can be found in the config file.

## Commands
`/hltweaker levels` - Show vanilla and Harvest Level Tweaker levels  <br>
`/hltweaker levels all` - Show all harvest levels that are registered in the game <br>
`/hltweaker overrides` - Show item harvest level overrides <br>

## Set Tinker's Construct Material Level
In your datapack for tinker's construct, set the harvest level of the material to `hltweaker:<level id>`


[1]: http://cf.way2muchnoise.eu/full_833035_downloads.svg
[2]: http://cf.way2muchnoise.eu/versions/833035_all.svg
[3]: https://www.curseforge.com/minecraft/mc-mods/harvest-level-tweaker
[4]: https://img.shields.io/discord/809053891466887169?label=support&logo=discord
[5]: https://discord.gg/FFAdyuqNvm
[6]: https://img.shields.io/github/license/yzl210/HarvestLevelTweaker?logo=github
[7]: https://github.com/yzl210/HarvestLevelTweaker/blob/1.18/LICENSE
[8]: https://img.shields.io/github/issues/yzl210/HarvestLevelTweaker?logo=github
[9]: https://github.com/yzl210/HarvestLevelTweaker/issues
[10]: https://img.shields.io/github/stars/yzl210/HarvestLevelTweaker?logo=github
[11]: https://github.com/yzl210/HarvestLevelTweaker/stargazers
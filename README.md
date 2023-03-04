[![][1]][3] [![][2]][3] [![][4]][5] [![][6]][7] [![][8]][9] [![][10]][11]

# Harvest Level Tweaker
A minecraft forge mod that allows modpack makers to add new harvest levels with custom names.

Currently supports:
* [The One Probe](https://www.curseforge.com/minecraft/mc-mods/the-one-probe)
* [Jade](https://www.curseforge.com/minecraft/mc-mods/jade)
* [WTHIT](https://www.curseforge.com/minecraft/mc-mods/wthit-forge)
* [Tinker's Construct](https://www.curseforge.com/minecraft/mc-mods/tinkers-construct)


## Add New Harvest Levels
Define new harvest levels in mod config `hltweaker-common.toml` <br>
Format: `<id>,<level>` (level start from 5, 0-4 are vanilla levels), example: `cobalt,5`

## Set Block Harvest Level
Create a datapack and add the block to block tag `hltweaker:needs_<level id>_tool`

## Override Item Harvest Level and Harvest Type
Override item harvest level and type in mod config `hltweaker-common.toml` <br>
Format: `<item id>,<mineable tag>+<maybe another mineable tag>+,<namespace and level id>` <br> 
Example: `minecraft:wooden_pickaxe,minecraft:mineable/pickaxe+minecraft:mineable/axe+minecraft:mineable/shovel,minecraft:netherite` <br>
If the level is from vanilla, use minecraft as namespace, otherwise use `hltweaker`

## Set Tinker's Construct Material Level
In your datapack for tinker's construct, set the harvest level of the material to `hltweaker:<level id>`

## Localization
Translation key for harvest level is `text.hltweaker.level.<level id>`
Translation key for tool type is `text.hltweaker.tool.<mod namespace>.<tool type>`

## Modify Vanilla Level Names
Set `enable_custom_vanilla_names` to true in mod config and create a resourcepack that has higher priority <br>
Translation key is `text.hltweaker.level.minecraft.<wood/gold/stone/iron/diamond/netherite>`

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
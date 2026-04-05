# Headstones
**Headstones** is a plugin that adds a simple mechanic to the game:
When a player dies, its inventory and experience will be **safely stored** in a _headstone_.

To get the stuff back, only the player who died can break its headstone.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/hs list` | `headstones.list` | Shows a numbered list of your active headstones with their coordinates. |
| `/hs tp <number>` | `headstones.tp` | Teleports you to the specified headstone. |
| `/hs reload` | `headstones.reload` | Reloads the configuration and messages. |
| `/hs cleardb` | `headstones.cleardb` | Clears all saved headstones data. |
| `/hs unplaced <list\|restore>` | `headstones.unplaced` | Manage and restore unplaced headstones. |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `headstones.list` | `true` | Allows listing your headstones. |
| `headstones.tp` | `true` | Allows teleporting to your headstones. |
| `headstones.keep-experience` | `true` | Controls if experience is stored in the headstone. |
| `headstones.keep-inventory` | `true` | Controls if inventory is stored in the headstone. |
| `headstones.allow-opponents`| `false` | Controls if the opponent can loot headstone. |
| `headstones.reload` | `op` | Allows reloading the plugin. |
| `headstones.cleardb` | `op` | Allows clearing the database. |
| `headstones.unplaced` | `op` | Allows managing unplaced headstones. |
| `headstones.disabled.<world_name>` | `false` | Disables headstones in a specific world. |

## Release Notes

### Version 1.3.0
- Added ability to disable headstones in specific worlds via permission `headstones.disabled.<world_name>`.

### Version 1.2.0
- Added `/hs tp <number>` command to teleport to headstones.

### Version 1.1.0
- Updated `/hs list` to show a numbered list.

## Credits
Author: alex3025

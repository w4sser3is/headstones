# Headstones

Don't lose your precious items when you die!
When a player dies, its inventory and experience will be safely stored in a headstone.
To get the stuff back, only the player who died can break its headstone (unless configured otherwise to allow looting).

## Commands

### Main Command
*   **/headstones**
    *   **Aliases:** `/hs`, `/headstone`
    *   **Description:** Shows the plugin's version and author information.

### Subcommands
*   **/headstones list**
    *   **Permission:** `headstones.list`
    *   **Description:** Lists all active headstones belonging to the player, including their world and coordinates.
*   **/headstones cleardb**
    *   **Permission:** `headstones.cleardb`
    *   **Description:** Clears the database of all headstones. This action makes all existing headstones useless (empty). Requires confirmation by running the command twice. (Players only)
*   **/headstones reload**
    *   **Permission:** `headstones.reload`
    *   **Description:** Reloads the configuration and translation files from disk.

## Permissions

*   `headstones.list`
    *   **Description:** Allows the player to see the coordinates of their headstones.
    *   **Default:** `true` (Everyone)
*   `headstones.keep-experience`
    *   **Description:** Allows the player to retrieve their experience when they break their headstone.
    *   **Default:** `true` (Everyone)
*   `headstones.keep-inventory`
    *   **Description:** Allows the player to retrieve their inventory items when they break their headstone.
    *   **Default:** `true` (Everyone)
*   `headstones.allow-opponents`
    *   **Description:** If a player has this permission, other players are allowed to break their headstone and loot the inventory/experience. The owner must be online for this to work.
    *   **Default:** `false` (Must be explicitly granted)
*   `headstones.reload`
    *   **Description:** Grants access to the `/headstones reload` command.
    *   **Default:** `op` (Operators only)
*   `headstones.cleardb`
    *   **Description:** Grants access to the `/headstones cleardb` command.
    *   **Default:** `op` (Operators only)

## Release Notes

### Latest Changes
*   **Added `/headstones list` command:** Players can now view a list of their active headstones and their locations.

## Credits

Original Creator: **alex3025**

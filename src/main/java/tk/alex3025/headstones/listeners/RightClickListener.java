package tk.alex3025.headstones.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import tk.alex3025.headstones.Headstones;
import tk.alex3025.headstones.utils.Headstone;
import tk.alex3025.headstones.utils.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RightClickListener extends ListenerBase {

    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getAction().isRightClick() && event.getHand().name().equals("HAND")) {
            Headstone headstone = Headstone.fromBlock(event.getClickedBlock(), event.getPlayer());

            if (headstone != null)
                new Message(event.getPlayer()).translation("headstone-info")
                        .replace("username", headstone.getOwner().getName())
                        .replace("datetime", new SimpleDateFormat(Headstones.getInstance().getConfig().getString("date-format")).format(new Date(headstone.getTimestamp())))
                        .send();
        }
    }

}

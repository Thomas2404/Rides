package tech.thomas2404.rides;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Rides extends JavaPlugin {

    //Sets up an array so that we can keep certain players from leaving the vehicle while it's moving. Also sets up for the bukkit runnable.
    Plugin plugin = this;
    ArrayList<String> ridingPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        //Registers the commands and listeners.
        this.getCommand("ride").setExecutor(new commandRide());
        getServer().getPluginManager().registerEvents(new listener(), this);
    }

    @Override
    public void onDisable() {
    }

    public class commandRide implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

            //Starts the ride if a player sent a command.
            if (sender instanceof Player) {

                Player player = (Player) sender;
                World world = player.getWorld();

                //Spawns the ride vehicle, and adds the player to the riding array.
                Location startLocation = new Location(Bukkit.getWorld("world"), 100, 4, 100);
                Minecart vehicle = (Minecart) world.spawnEntity(startLocation, EntityType.MINECART);
                ridingPlayers.add(player.getName());
                new BukkitRunnable() {

                    //Sets up some math for the ride path.
                    double angle1 = 0D;
                    double angle2= 0D;
                    double step1 = ((2 * Math.PI) / 150.0D);
                    double speed1 = 0.5D;
                    @Override
                    public void run() {

                        //Move the ride vehicle in a circle. Uses sin and cos to make the circle path, and move the vehicle up and down.
                        vehicle.setVelocity(new Vector(Math.cos(angle1 + 1.5) * speed1, Math.cos(angle2) * speed1, Math.sin(angle1 + 1.5) * speed1));
                        angle1 += step1;
                        angle2 += 0.2;
                        //If the vehicle has gone in a circle twice, stop the ride and despawn the vehicle.
                        if (angle1 > 13) {
                            this.cancel();
                            ridingPlayers.remove(player.getName());
                            vehicle.remove();
                        }
                    }
                }.runTaskTimer(plugin, 0, 1);

            } else {
                //Command was sent by a console, print this message.
                sender.sendMessage("Console can not start a ride.");
                return false;
            }
            return false;
        }
    }

    //Sets up the listener so that players cant leave the ride vehicle while riding the ride.
    public class listener implements Listener {
        @EventHandler
        public void exitVehicle(VehicleExitEvent event) {
            //If the player is riding and tries to exit the vehicle, cancel the event.
            String player = event.getExited().getName();
            if (ridingPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }
    }
}

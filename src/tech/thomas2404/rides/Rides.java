package tech.thomas2404.rides;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Rides extends JavaPlugin {

    //Sets up an array so that we can keep certain players from leaving the vehicle while it's moving. Also sets up for the bukkit runnable.
    private Plugin plugin = this;
    private ArrayList<String> ridingPlayers = new ArrayList<>();
    private ArrayList<String> countingDown = new ArrayList<>();
    private Boolean startCountdown = false;
    private Boolean riding = false;
    private Boolean cancelCountDown = false;
    private  Boolean spawnedVehicle;
    private int x = 0;
    private int y = 0;
    private int z = 0;

    @Override
    public void onEnable() {
        //Registers the commands and listeners.
        this.getCommand("ride").setExecutor(new CommandRide());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        //Stuff to do when the plugin is disabled.
    }

    public class CommandRide implements CommandExecutor {

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

                //Waits a second before moving the vehicle so a player can get in.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        z++;
                        if (z >= 2) {
                            z = 0;
                            this.cancel();

                            //Starts moving the vehicle.
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
                                        riding = false;
                                    }
                                }
                            }.runTaskTimer(plugin, 0, 1);
                        }
                    }
                }.runTaskTimer(plugin, 20L, 20L);


            } else {
                //Command was sent by a console, print this message.
                sender.sendMessage("Console can not start a ride.");
                return false;
            }
            return false;
        }
    }

    //Sets up the listener so that players cant leave the ride vehicle while riding the ride.
    public class EventListener implements Listener {

        @EventHandler
        public void exitVehicle(VehicleExitEvent event) {
            //If the player is riding and tries to exit the vehicle, cancel the event.
            String player = event.getExited().getName();
            if (ridingPlayers.contains(player)) {
                event.setCancelled(true);
            }
        }

        //Handles the countdown to spawn the ride.
        @EventHandler
        public void NearRide(PlayerMoveEvent event) {

            //Sets up some variables for the countdown.
            Player player = event.getPlayer();
            Location location = new Location(Bukkit.getWorld("world"), 100 , 4, 100);
            double distance = location.distance(player.getLocation());

            //If the player is within 5 blocks, start the countdown.
            if (distance < 5 && !startCountdown && !riding) {

                //Sets up some stuff for the countdown, and the text screen.
                startCountdown = true;
                countingDown.add(player.getName());
                World world = player.getWorld();
                Location screenLocation = new Location (Bukkit.getWorld("world"), 100, 4, 100);
                ArmorStand screen = (ArmorStand) world.spawnEntity(screenLocation, EntityType.ARMOR_STAND);
                screen.setVisible(false);
                screen.setCustomName(ChatColor.GRAY + "The ride will spawn in " + ChatColor.RED + "5" + ChatColor.GRAY + " seconds! Please don't leave!");
                screen.setCustomNameVisible(true);
                y = 5;
                x = 0;
                    //Starts the countdown.
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        x++;
                        y--;
                        screen.setCustomName(ChatColor.GRAY + "The ride will spawn in " + ChatColor.RED + y + ChatColor.GRAY + " seconds! Please don't leave!");

                        //If the countdown is cancelled, cancel it.
                        if (cancelCountDown) {
                            startCountdown = false;
                            countingDown.remove(player.getName());
                            screen.remove();
                            cancelCountDown = false;
                            this.cancel();
                            x = 0;
                            y = 5;
                        }
                        //If the countdown has gone for 5 seconds, spawn the ride vehicle.
                        if (x >= 5) {
                            player.chat("/ride");
                            countingDown.remove(player.getName());
                            startCountdown = false;
                            riding = true;
                            screen.remove();
                            this.cancel();
                            x = 0;
                            y = 5;
                        }
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }

            //If the player leaves the loading area before the countdown finishes, end the countdown.
            if ((distance > 5) && countingDown.contains(player.getName())) {
                cancelCountDown = true;
                countingDown.remove(player.getName());

            }
        }
    }
}

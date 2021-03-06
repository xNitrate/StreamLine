package net.plasmere.streamline;

import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.plasmere.streamline.config.Config;
import net.plasmere.streamline.config.ConfigUtils;
import net.plasmere.streamline.discordbot.MessageListener;
import net.plasmere.streamline.discordbot.ReadyListener;
import net.plasmere.streamline.events.Event;
import net.plasmere.streamline.events.EventsHandler;
import net.plasmere.streamline.events.EventsReader;
import net.plasmere.streamline.objects.Guild;
import net.plasmere.streamline.objects.configs.Lobbies;
import net.plasmere.streamline.objects.configs.ServerPermissions;
import net.plasmere.streamline.objects.timers.GuildXPTimer;
import net.plasmere.streamline.objects.timers.PlayerClearTimer;
import net.plasmere.streamline.objects.timers.PlayerXPTimer;
import net.plasmere.streamline.objects.timers.PlaytimeTimer;
import net.plasmere.streamline.utils.*;
import net.plasmere.streamline.utils.holders.ViaHolder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.plugin.Plugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StreamLine extends Plugin /*implements Runnable*/ {

	private static StreamLine instance = null;

	public static Config config;
	public static ServerPermissions serverPermissions;
	public static Lobbies lobbies;
	public static ViaHolder viaHolder;

	private static JDA jda = null;
	private static boolean isReady = false;

	private static final EmbedBuilder eb = new EmbedBuilder();

	private final File plDir = new File(getDataFolder() + File.separator + "players" + File.separator);
	private final File gDir = new File(getDataFolder() + File.separator + "guilds" + File.separator);
	private final File confDir = new File(getDataFolder() + File.separator + "configs" + File.separator);
	private File eventsDir;

	private ScheduledTask guilds;
	private ScheduledTask players;
	private ScheduledTask cachedPlayers;
	private ScheduledTask playtime;

	public StreamLine(){
		instance = this;
	}

	public final File getPlDir() {
		return plDir;
	}
	public final File getGDir() {
		return gDir;
	}
	public final File getEDir() { return eventsDir; }

    private void init(){
		if (jda != null) try { jda.shutdownNow(); jda = null; } catch (Exception e) { e.printStackTrace();}

		try {
			JDABuilder jdaBuilder = JDABuilder.createDefault(ConfigUtils.botToken)
					.setActivity(Activity.playing(ConfigUtils.botStatusMessage));
			jdaBuilder.addEventListeners(
					new MessageListener(this),
					new ReadyListener(this)
			);
			jda = jdaBuilder.build().awaitReady();
		} catch (Exception e) {
			getLogger().warning("An unknown error occurred building JDA...");
			return;
		}

		if (jda.getStatus() == JDA.Status.CONNECTED) {
			isReady = true;

			getLogger().info("JDA status is connected...");
		}
	}

	public void loadGuilds(){
		if (! gDir.exists()) {
			try {
				gDir.mkdir();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void loadPlayers(){
		if (! plDir.exists()) {
			try {
				plDir.mkdir();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public void loadEvents(){
		if (! ConfigUtils.tagsEvents) return;

		eventsDir = new File(getDataFolder() + File.separator + ConfigUtils.tagsEventsFolder + File.separator);

		if (! eventsDir.exists()) {
			try {
				eventsDir.mkdir();
			} catch (Exception e){
				e.printStackTrace();
			}
		}

		if (ConfigUtils.tagsEventsWhenEmpty) {
			try	(InputStream in = getResourceAsStream("default.event")) {
				Files.copy(in, Path.of(eventsDir.toPath() + File.separator + "default.event"));
			} catch (FileAlreadyExistsException e){
				getLogger().info("Default event file already here, skipping...");
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		try {
			List<Path> files = Files.walk(eventsDir.toPath()).filter(p -> p.toString().endsWith(".event")).collect(Collectors.toList());

			for (Path file : files) {
				Event event = EventsReader.fromFile(file.toFile());

				if (event == null) continue;

				EventsHandler.addEvent(event);
			}

			getLogger().info("Loaded " + EventsHandler.getEvents().size() + " events into memory!");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void loadTimers(){
		try {
			guilds = getProxy().getScheduler().schedule(this, new GuildXPTimer(ConfigUtils.timePerGiveG), 1, 1, TimeUnit.SECONDS);
			players = getProxy().getScheduler().schedule(this, new PlayerXPTimer(ConfigUtils.timePerGiveP), 1, 1, TimeUnit.SECONDS);
			cachedPlayers = getProxy().getScheduler().schedule(this, new PlayerClearTimer(ConfigUtils.cachedPClear), 1, 1, TimeUnit.SECONDS);
			playtime = getProxy().getScheduler().schedule(this, new PlaytimeTimer(1), 1, 1, TimeUnit.SECONDS);
			getLogger().info("Loaded 3 timers (Runnables) into memory...!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadServers(){
		if (! confDir.exists()) {
			try {
				confDir.mkdir();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		serverPermissions = new ServerPermissions(true);

		if (ConfigUtils.lobbies) {
			lobbies = new Lobbies(true);
		}
	}

    public void onLoad(){
    	InstanceHolder.setInst(instance);
	}

	@Override
	public void onEnable(){
		instance = this;

		// Teller.
		getLogger().info("Loading version [v" + getProxy().getPluginManager().getPlugin("StreamLine").getDescription().getVersion() + "]...");

		// Via Support.
		viaHolder = new ViaHolder();

		// Config.
		config = new Config();

		// Commands.
		PluginUtils.loadCommands(this);

		// Listeners.
		PluginUtils.loadListeners(this);

		// JDA init.
        Thread initThread = new Thread(this::init, "Streamline - Initialization");
        initThread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            getLogger().severe("Streamline failed to load properly: " + e.getMessage() + ".");
        });
        initThread.start();

        // Guilds.
		loadGuilds();

		// Players.
		loadPlayers();

		// Timers.
		loadTimers();

		// Events.
		loadEvents();

		// Servers by Versions.
		if (viaHolder.enabled) {
			loadServers();
		} else {
			getLogger().severe("Streamline server custom configs have been disabled due to no ViaVersion being detected.");
		}
	}

	@Override
	public void onDisable() {
		guilds.cancel();
		players.cancel();
		playtime.cancel();
		cachedPlayers.cancel();

		try {
			if (jda != null) {
				if (ConfigUtils.moduleShutdowns)
					try {
						Objects.requireNonNull(jda.getTextChannelById(ConfigUtils.textChannelOfflineOnline)).sendMessage(eb.setDescription("Bot shutting down...!").build()).queue();
					} catch (Exception e){
						getLogger().warning("An unknown error occurred with sending online message: " + e.getMessage());
					}

				Thread.sleep(2000);

				jda.getEventManager().getRegisteredListeners().forEach(listener -> jda.getEventManager().unregister(listener));
				CompletableFuture<Void> shutdownTask = new CompletableFuture<>();
				jda.addEventListener(new ListenerAdapter() {
					@Override
					public void onShutdown(@NotNull ShutdownEvent event) {
						shutdownTask.complete(null);
					}
				});
				jda.shutdownNow();
				jda = null;

				try {
					shutdownTask.get(5, TimeUnit.SECONDS);
				} catch (Exception e) {
					getLogger().warning("JDA took too long to shutdown, skipping!");
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		saveGuilds();
	}

	public void saveGuilds(){
		for (Guild guild : GuildUtils.getGuilds()){
			try {
				guild.saveInfo();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		getLogger().info("Saved " + GuildUtils.getGuilds().size() + " Guilds!");
	}

    public static Config getConfig() { return config; }
	public static StreamLine getInstance() { return instance; }
	public static JDA getJda() { return jda; }
	public static boolean getIsReady() { return isReady; }

	public static void setReady(boolean ready) { isReady = ready; }

}

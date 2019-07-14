package net.teamfruit.cavern2leaderboards;

import static net.teamfruit.cavern2leaderboards.Cavern2Leaderboards.*;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Kamesuta
 */
@Config(modid = MODID, name = "cavern/leaderboards") //--> /config/cavern/leaderboards.cfg
public class Cavern2LeaderboardsConfig {

	@Config.RequiresMcRestart
	@Config.Name("Unit Miner Rank")
	@Config.Comment({ "mining level: Stone Miner ...% (... XP)" })
	public static String unitMinerRankWithProgress = "%s \u00A77%.2f%%";

	@Config.RequiresMcRestart
	@Config.Name("Unit Miner Rank Without Progress")
	@Config.Comment({ "mining level: Stone Miner (... XP)" })
	public static String unitMinerRankWithoutProgress = "%s \u00A77(%d XP)";

	@Config.RequiresMcRestart
	@Config.Name("Miner Rank Leaderboards Name")
	@Config.Comment({ "Leaderboards Name" })
	public static String minerRankLeaderboardsName = "Cavern II Miner Rank";

	@Config.Name("debug mode")
	@Config.Comment("enable more verbose output of what's going on")
	public static boolean debugMode = false;

	@Mod.EventBusSubscriber(modid = MODID)
	public static class Handler {

		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(MODID))
				ConfigManager.load(MODID, Config.Type.INSTANCE);
		}
	}
}

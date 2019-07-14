package net.teamfruit.cavern2leaderboards;

import static net.teamfruit.cavern2leaderboards.Cavern2Leaderboards.*;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftbutilities.data.Leaderboard;
import com.feed_the_beast.ftbutilities.events.LeaderboardRegistryEvent;

import cavern.api.IMinerStats;
import cavern.capability.CapabilityMinerStats;
import cavern.capability.CaveCapabilities;
import cavern.stats.MinerRank;
import cavern.stats.MinerStats;
import cavern.util.CaveUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(certificateFingerprint = FINGERPRINT_KEY, name = MODNAME, version = VERSION, acceptedMinecraftVersions = MCVERSIONS, modid = MODID, dependencies = DEPENDENCIES, serverSideOnly = true, acceptableRemoteVersions = "*")
public class Cavern2Leaderboards {

	//Version
	public static final String MCVERSIONS = "[1.12, 1.13)";
	public static final String VERSION = "@VERSION@";

	//Meta Information
	public static final String MODNAME = "Cavern2 Leaderboards";
	public static final String MODID = "cavern2leaderboards";
	public static final String DEPENDENCIES = "after:voting@[1.3.0,)";

	public static final String FINGERPRINT_KEY = "@FINGERPRINTKEY@";
	private static final Logger log = LogManager.getLogger(MODID);

	public static Logger getLogger() {
		return log;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(Cavern2Leaderboards.class);
	}

	public static boolean isDebugMode() {
		return Cavern2LeaderboardsConfig.debugMode;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
	}

	@Mod.EventHandler
	public void onServerStarted(FMLServerStartedEvent event) {
	}

	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
	}

	private static IMinerStats getMinerStats(ForgePlayer player) {
		IMinerStats stats = null;
		if (player.isOnline()) {
			stats = MinerStats.get(player.entityPlayer, true);
		} else {
			NBTTagCompound playerNbt = player.getPlayerNBT();
			if (playerNbt.hasKey("ForgeCaps")) {
				NBTTagCompound capsNbt = playerNbt.getCompoundTag("ForgeCaps");
				String minerStatsKey = CaveUtils.getKey("miner_stats").toString();
				if (capsNbt.hasKey(minerStatsKey)) {
					CapabilityMinerStats cap = new CapabilityMinerStats(null);
					cap.deserializeNBT(capsNbt.getCompoundTag(minerStatsKey));
					stats = cap.getCapability(CaveCapabilities.MINER_STATS, null);
				}
			}
		}
		if (stats != null &&
				(stats.getRank() >= 0 && stats.getPoint() >= 0) &&
				(stats.getRank() > 0 || stats.getPoint() > 0))
			return stats;
		return null;
	}

	@SubscribeEvent
	public static void registerLeaderboards(LeaderboardRegistryEvent event) {
		event.register(new Leaderboard(
				new ResourceLocation(MODID, "minerrank"),
				new TextComponentString(Cavern2LeaderboardsConfig.minerRankLeaderboardsName),
				player -> {
					ITextComponent componentMessage = new TextComponentString("");
					IMinerStats stats = getMinerStats(player);

					if (stats != null) {
						String tempKeyword = "${MINER_RANK}";
						MinerRank minerRank = MinerRank.get(stats.getRank());
						MinerRank nextRank = MinerRank.get(stats.getRank() + 1);
						String formattedMessage;
						if (minerRank.getRank() < nextRank.getRank()) {
							double per = stats.getPoint() == 0 ? 0.0D
									: (double) stats.getPoint() / (double) nextRank.getPhase() * 100.0D;
							formattedMessage = String.format(
									Cavern2LeaderboardsConfig.unitMinerRankWithProgress,
									tempKeyword,
									per, stats.getPoint());
						} else {
							formattedMessage = String.format(
									Cavern2LeaderboardsConfig.unitMinerRankWithoutProgress,
									tempKeyword,
									stats.getPoint());
						}
						if (StringUtils.contains(formattedMessage, tempKeyword)) {
							ITextComponent minerrank = new TextComponentTranslation(
									minerRank.getUnlocalizedName());
							String[] splitted = StringUtils.splitByWholeSeparatorPreserveAllTokens(
									formattedMessage,
									tempKeyword);
							for (int i = 0; i < splitted.length; i++) {
								TextComponentString message = new TextComponentString(splitted[i]);
								if (i == 0)
									componentMessage = message;
								else
									componentMessage = componentMessage.appendSibling(minerrank)
											.appendSibling(message);
							}
						} else {
							componentMessage = new TextComponentString(formattedMessage);
						}
					}
					return componentMessage;
				},
				Comparator.comparing(player -> {
					return getMinerStats(player);
				}, (statsA, statsB) -> {
					if (statsA == null && statsB == null)
						return 0;
					if (statsA == null)
						return 1;
					if (statsB == null)
						return -1;
					int c = Integer.compare(statsA.getRank(), statsB.getRank());
					if (c == 0)
						c = Integer.compare(statsA.getPoint(), statsB.getPoint());
					return -c;
				}),
				player -> {
					return getMinerStats(player) != null;
				}));
	}
}

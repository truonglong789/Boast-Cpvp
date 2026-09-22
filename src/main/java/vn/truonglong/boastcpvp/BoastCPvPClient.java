package vn.truonglong.boastcpvp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BoastCPvPClient implements ClientModInitializer {
    private static final long DEFAULT_INTERVAL_MS = 60_000L;
    private static long lastSent = 0L;
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();
    private static String webhook = "";
    private static long intervalMs = DEFAULT_INTERVAL_MS;

    @Override
    public void onInitializeClient() {
        loadConfig();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dn").then(ClientCommandManager.argument("message", StringArgumentType.greedyString()).executes(context -> { String message = StringArgumentType.getString(context, "message"); sendMessageToDiscord(message); return 1; }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || webhook.isBlank()) return;

            long now = System.currentTimeMillis();
            if (now - lastSent >= intervalMs) {
                lastSent = now;
                sendCoordinates(client);
            }
        });
    }

    private static void sendMessageToDiscord(String message) { if (webhook.isBlank()) return; JsonObject body = new JsonObject(); body.addProperty("username", "Boast CPvP"); body.addProperty("content", message); HttpRequest request = HttpRequest.newBuilder().uri(URI.create(webhook)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).build(); HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding()); }

    private static void sendCoordinates(MinecraftClient client) {
        ClientPlayerEntity p = client.player;
        if (p == null) return;

        String name = p.getName().getString();
        String dimension = client.world.getRegistryKey().getValue().toString();

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "📍 Tọa độ người chơi");
        embed.addProperty("description",
                "**Tên:** " + name + "\n" +
                "**X:** " + Math.round(p.getX()) + "\n" +
                "**Y:** " + Math.round(p.getY()) + "\n" +
                "**Z:** " + Math.round(p.getZ()) + "\n" +
                "**Dimension:** " + dimension);

        JsonObject body = new JsonObject();
        body.addProperty("username", "Boast CPvP");
        body.add("embeds", GSON.toJsonTree(new JsonObject[]{embed}));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhook))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HTTP.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .exceptionally(ex -> {
                    client.execute(() -> client.player.sendMessage(
                            Text.literal("§cBoast CPvP: không gửi được Discord."), false));
                    return null;
                });
    }

    private static void loadConfig() {
        try {
            Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("config");
            Files.createDirectories(dir);
            Path file = dir.resolve("boastcpvp.properties");

            Properties p = new Properties();
            if (Files.exists(file)) {
                try (var in = Files.newInputStream(file)) { p.load(in); }
            }

            webhook = p.getProperty("webhook", "").trim();
            intervalMs = Long.parseLong(p.getProperty("interval_seconds", "60")) * 1000L;

            if (!Files.exists(file)) {
                p.setProperty("webhook", https://discord.com/api/webhooks/1551953377993166898/8cOU4EvCgulCIt_HYyLi_obWhfzjjO4Tgx0eZWFVvAdTVis078GEmmz41ajWAOpABmqk"");
                p.setProperty("interval_seconds", "60");
                try (var out = Files.newOutputStream(file)) { p.store(out, "Boast CPvP configuration"); }
            }
        } catch (Exception ignored) {
            webhook = "";
            intervalMs = DEFAULT_INTERVAL_MS;
        }
    }
}

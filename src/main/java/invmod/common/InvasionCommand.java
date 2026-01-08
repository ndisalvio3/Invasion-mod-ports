package invmod.common;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import invmod.Invasion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class InvasionCommand {
    private InvasionCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("invasion")
            .then(Commands.literal("help")
                .executes(ctx -> {
                    send(ctx.getSource(), "--- Showing Invasion help page 1 of 1 ---");
                    send(ctx.getSource(), "/invasion begin <wave> to start a wave");
                    send(ctx.getSource(), "/invasion end to end the invasion");
                    send(ctx.getSource(), "/invasion range <radius> to set the spawn range");
                    return 1;
                }))
            .then(Commands.literal("begin")
                .then(Commands.argument("wave", IntegerArgumentType.integer(1))
                    .executes(ctx -> {
                        int startWave = IntegerArgumentType.getInteger(ctx, "wave");
                        if (Invasion.getFocusNexus() != null) {
                            Invasion.getFocusNexus().debugStartInvaion(startWave);
                        }
                        return 1;
                    })))
            .then(Commands.literal("end")
                .executes(ctx -> {
                    String username = ctx.getSource().getTextName();
                    if (Invasion.getActiveNexus() != null) {
                        Invasion.getActiveNexus().emergencyStop();
                        Invasion.broadcastToAll(username + " ended invasion");
                    } else {
                        send(ctx.getSource(), username + ": No invasion to end");
                    }
                    return 1;
                }))
            .then(Commands.literal("range")
                .then(Commands.argument("radius", IntegerArgumentType.integer(32, 128))
                    .executes(ctx -> {
                        int radius = IntegerArgumentType.getInteger(ctx, "radius");
                        String username = ctx.getSource().getTextName();
                        if (Invasion.getFocusNexus() != null) {
                            if (Invasion.getFocusNexus().setSpawnRadius(radius)) {
                                send(ctx.getSource(), "Set nexus range to " + radius);
                            } else {
                                send(ctx.getSource(), username + ": Can't change range while nexus is active");
                            }
                        } else {
                            send(ctx.getSource(), username + ": Right-click the nexus first to set target for command");
                        }
                        return 1;
                    })))
            .then(Commands.literal("nexusstatus")
                .executes(ctx -> {
                    if (Invasion.getFocusNexus() != null) {
                        Invasion.getFocusNexus().debugStatus();
                    }
                    return 1;
                }))
            .then(Commands.literal("bolt")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                    .then(Commands.argument("y", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                            .then(Commands.argument("time", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    runBolt(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"), IntegerArgumentType.getInteger(ctx, "time"));
                                    return 1;
                                }))
                            .executes(ctx -> {
                                runBolt(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z"), 40);
                                return 1;
                            })))
                    .executes(ctx -> {
                        runBolt(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "x"), IntegerArgumentType.getInteger(ctx, "y"), 0, 40);
                        return 1;
                    })))
            .then(Commands.literal("status")
                .executes(ctx -> {
                    if (Invasion.getFocusNexus() != null) {
                        send(ctx.getSource(), "nexus status:" + Invasion.getFocusNexus().isActive());
                    }
                    return 1;
                }))
        );
    }

    private static void runBolt(CommandSourceStack source, int dx, int dy, int dz, int time) {
        if (Invasion.getFocusNexus() == null) {
            return;
        }
        int x = Invasion.getFocusNexus().getXCoord() + dx;
        int y = Invasion.getFocusNexus().getYCoord() + dy;
        int z = Invasion.getFocusNexus().getZCoord() + dz;
        Invasion.getFocusNexus().createBolt(x, y, z, time);
    }

    private static void send(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }
}

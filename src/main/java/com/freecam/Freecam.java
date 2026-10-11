package com.freecam;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
    public static boolean active = false;
    public static double x, y, z;
    public static float yaw, pitch;
    public static int cameraFrames = 0;

    private static long last;
    private static int activeTicks;
    private static Object greeted;

    private static final double BASE_SPEED = 12.0; // blocks per second
    private static final double SPRINT_MULT = 3.0;

    public static void onTick(Minecraft mc) {
        // One-time message per world join so you know the mod loaded
        if (mc.player != null && greeted != mc.player) {
            greeted = mc.player;
            mc.player.displayClientMessage(
                Component.literal("[Freecam] loaded. Press F5 to toggle."), false);
        }

        // Read the toggle key. Our key and vanilla's F5 can share a key, so drain both.
        boolean pressed = false;
        while (FreecamClient.TOGGLE.consumeClick()) pressed = true;
        KeyMapping vanilla = mc.options.keyTogglePerspective;
        if (FreecamClient.TOGGLE.saveString().equals(vanilla.saveString())) {
            while (vanilla.consumeClick()) pressed = true;
        }
        if (pressed && mc.screen == null && mc.player != null && mc.level != null) {
            toggle(mc);
        }

        if (!active) return;

        // Turn off on death / disconnect
        if (mc.player == null || mc.level == null || !mc.player.isAlive()) {
            active = false;
            return;
        }

        // Diagnostic: warn if the camera hook never ran
        activeTicks++;
        if (activeTicks == 20 && cameraFrames == 0) {
            mc.player.displayClientMessage(
                Component.literal("[Freecam] Camera hook did not apply - check latest.log"), false);
        }

        // Don't let clicks act while in freecam
        while (mc.options.keyAttack.consumeClick()) { }
        while (mc.options.keyUse.consumeClick()) { }
    }

    private static void toggle(Minecraft mc) {
        if (active) {
            active = false;
            mc.player.displayClientMessage(Component.literal("Freecam OFF"), true);
            return;
        }
        Vec3 eye = mc.player.getEyePosition();
        x = eye.x;
        y = eye.y;
        z = eye.z;
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();
        last = System.nanoTime();
        cameraFrames = 0;
        activeTicks = 0;
        active = true;
        mc.player.displayClientMessage(Component.literal("Freecam ON"), true);
    }

    /** Mouse look: steers the camera instead of the player. */
    public static void look(double dYaw, double dPitch) {
        yaw += (float) dYaw * 0.15f;
        pitch += (float) dPitch * 0.15f;
        if (pitch > 90f) pitch = 90f;
        if (pitch < -90f) pitch = -90f;
    }

    /** Called once per rendered frame while active (from the camera mixin). */
    public static void update() {
        cameraFrames++;
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        double dt = Math.min((now - last) / 1.0e9, 0.1);
        last = now;
        if (mc.screen != null) return;

        Options o = mc.options;
        double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
        double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        double v = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);
        double speed = BASE_SPEED * (o.keySprint.isDown() ? SPRINT_MULT : 1.0) * dt;

        double yr = Math.toRadians(yaw);
        double pr = Math.toRadians(pitch);
        double fx = -Math.sin(yr) * Math.cos(pr);
        double fy = -Math.sin(pr);
        double fz = Math.cos(yr) * Math.cos(pr);
        double lx = Math.cos(yr);
        double lz = Math.sin(yr);

        x += (fx * f + lx * s) * speed;
        y += (fy * f + v) * speed;
        z += (fz * f + lz * s) * speed;
    }
}

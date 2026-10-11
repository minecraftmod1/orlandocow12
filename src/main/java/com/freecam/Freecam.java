package com.freecam;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
    public static boolean active = false;
    private static RemotePlayer cam;
    private static float yaw, pitch;
    private static Object greeted;

    private static final double BASE_SPEED = 0.6; // blocks per tick
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

        tick(mc);
    }

    private static void toggle(Minecraft mc) {
        if (active) {
            off(mc, true);
            return;
        }
        cam = new RemotePlayer(mc.level, mc.player.getGameProfile());
        Vec3 eye = mc.player.getEyePosition();
        yaw = mc.player.getYRot();
        pitch = mc.player.getXRot();

        double feetY = eye.y - cam.getEyeHeight();
        cam.setPos(eye.x, feetY, eye.z);
        cam.xo = eye.x;
        cam.yo = feetY;
        cam.zo = eye.z;
        applyRotation();

        mc.setCameraEntity(cam);
        active = true;
        mc.player.displayClientMessage(Component.literal("Freecam ON"), true);
    }

    private static void off(Minecraft mc, boolean message) {
        active = false;
        cam = null;
        if (mc.player != null) {
            mc.setCameraEntity(mc.player);
            if (message) mc.player.displayClientMessage(Component.literal("Freecam OFF"), true);
        }
    }

    /** Mouse look: steers the camera instead of the player. */
    public static void look(double dYaw, double dPitch) {
        if (cam == null) return;
        yaw += (float) dYaw * 0.15f;
        pitch += (float) dPitch * 0.15f;
        if (pitch > 90f) pitch = 90f;
        if (pitch < -90f) pitch = -90f;
        applyRotation();
    }

    private static void applyRotation() {
        cam.setYRot(yaw);
        cam.setXRot(pitch);
        cam.yRotO = yaw;
        cam.xRotO = pitch;
    }

    private static void tick(Minecraft mc) {
        if (!active) return;

        if (cam == null || mc.player == null || mc.level == null
                || !mc.player.isAlive() || mc.getCameraEntity() != cam) {
            off(mc, false);
            return;
        }

        // Don't let clicks act on whatever the camera is pointing at
        while (mc.options.keyAttack.consumeClick()) { }
        while (mc.options.keyUse.consumeClick()) { }

        cam.xo = cam.getX();
        cam.yo = cam.getY();
        cam.zo = cam.getZ();

        if (mc.screen != null) return;

        Options o = mc.options;
        double f = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
        double s = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
        double v = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);
        double speed = BASE_SPEED * (o.keySprint.isDown() ? SPRINT_MULT : 1.0);

        double yr = Math.toRadians(yaw);
        double pr = Math.toRadians(pitch);
        double fx = -Math.sin(yr) * Math.cos(pr);
        double fy = -Math.sin(pr);
        double fz = Math.cos(yr) * Math.cos(pr);
        double lx = Math.cos(yr);
        double lz = Math.sin(yr);

        cam.setPos(
            cam.getX() + (fx * f + lx * s) * speed,
            cam.getY() + (fy * f + v) * speed,
            cam.getZ() + (fz * f + lz * s) * speed
        );
    }
}

package com.freecam;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class FreecamClient implements ClientModInitializer {
    public static KeyMapping TOGGLE;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath("freecam", "main"));

        TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.freecam.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F5,
            category
        ));

        ClientTickEvents.START_CLIENT_TICK.register(Freecam::onTick);
    }
}

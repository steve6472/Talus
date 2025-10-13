package steve6472.talus.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import steve6472.talus.TalusClient;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class ElytraSwap
{
    public static final String KEY_ELYTRA = "key.talus.swap_elytra";

    private static KeyMapping BIND;
    private static boolean pressed;

    private static final int ARMOR_SLOT = 6;
    private static final int OFFHAND_SLOT = 45;

    public static void init()
    {
        BIND = KeyBindingHelper.registerKeyBinding(new KeyMapping(KEY_ELYTRA, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, TalusClient.TALUS_KEY_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> elytraSwap());
    }

    // TODO: select elytra (glider) with most durability
    private static void elytraSwap()
    {
        if (!BIND.isDown())
        {
            pressed = false;
            return;
        }

        if (pressed)
            return;

        pressed = true;

        if (isOnHypixel())
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (player == null || gameMode == null)
            return;

        int firstSlot = ARMOR_SLOT;
        int swapSlot = -1;

        Inventory playerInventory = player.getInventory();
        NonNullList<ItemStack> inventory = playerInventory.getNonEquipmentItems();
        ItemStack chestplateSlot = player.getItemBySlot(EquipmentSlot.CHEST);

        boolean wearingNothing = chestplateSlot.getItem() == Items.AIR;
        boolean wearingElytra = !wearingNothing && isElytra(chestplateSlot);
        boolean wearingChestplate = !wearingElytra;

        ItemStack offhandSlot = player.getOffhandItem();
        if (fitsChestplate(player, offhandSlot) && (wearingNothing || wearingChestplate && isElytra(offhandSlot) || wearingElytra))
        {
            firstSlot = OFFHAND_SLOT;
            swapSlot = 38;
        }

        if (swapSlot == -1)
        {
            for (ItemStack itemStack : inventory)
            {
                Item item = itemStack.getItem();
                if (item == Items.AIR || !fitsChestplate(player, itemStack))
                {
                    continue;
                }

                boolean isElytra = isElytra(itemStack);
                if (wearingNothing || wearingElytra && !isElytra || wearingChestplate)
                {
                    swapSlot = inventory.indexOf(itemStack);
                }
            }
        }

        // Fix hotbar
        if (swapSlot != -1 && swapSlot < 9)
            swapSlot += 36;

        if (swapSlot == -1)
            return;

        gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, firstSlot, 0, ClickType.PICKUP, player);
        gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, swapSlot, 0, ClickType.PICKUP, player);
        gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, firstSlot, 0, ClickType.PICKUP, player);
    }

    private static boolean isOnHypixel()
    {
        // Disable on Hypixel
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null)
        {
            String brand = connection.serverBrand();
            if (brand != null && brand.contains("Hypixel"))
            {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null)
                {
                    player.displayClientMessage(Component.translatable("talus.elytra_swap.no_hypixel"), false);
                }
                return true;
            }
        }

        return false;
    }

    private static boolean fitsChestplate(Player player, ItemStack itemStack)
    {
        return itemStack.getItem() != Items.AIR && player.getEquipmentSlotForItem(itemStack) == EquipmentSlot.CHEST;
    }

    private static boolean isElytra(ItemStack itemStack)
    {
        // TODO: change to glider component instead
        return itemStack.getItem() == Items.ELYTRA;
    }
}

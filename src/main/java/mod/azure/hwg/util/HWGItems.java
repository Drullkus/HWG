package mod.azure.hwg.util;

import mod.azure.hwg.HWGMod;
import mod.azure.hwg.item.BulletAmmo;
import mod.azure.hwg.item.PistolItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class HWGItems {

	public static PistolItem PISTOL = item(new PistolItem(), "pistol");
	public static BulletAmmo BULLETS = item(new BulletAmmo(1.2F), "bullets");

	static <T extends Item> T item(T c, String id) {
		Registry.register(Registry.ITEM, new Identifier(HWGMod.MODID, id), c);
		return c;
	}

	static <T extends Item> T item(String id, T c) {
		Registry.register(Registry.ITEM, new Identifier(HWGMod.MODID, id), c);
		return c;
	}
}

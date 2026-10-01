package com.hbm.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class ObjModelGameTest implements FabricClientGameTest {

	private static final String PRESS = "hbm:tile.machine_press[item_model=\"hbm:render/13749918a6cf7c3a674bdaadef5dac4b\"]";
	private static final String THRUSTER = "hbm:item.mp_thruster_10_kerosene[item_model=\"hbm:render/3eb3b9dd252bc85973e870690c0bc2b7__missile_parts_mp_t_10_kerosene__missile_parts_thrusters_mp_t_10_kerosene\"]";
	private static final String COMPOSITE_BOBBLEHEAD = "hbm:tile.bobblehead[item_model=\"hbm:render/00d616fc0f0ef5f193ad84ae05043cbd__composite_fd4981d39f4cbde66dd8\"]";
	private static final String ORDINARY_BLOCK = "hbm:block_steel";
	private static final String RBMK_BLOCK = "hbm:rbmk_absorber";
	private static final String C4_BLOCK = "hbm:c4";
	private static final String LIGHTSTONE_BLOCK = "hbm:lightstone__4";
	private static final String CONCRETE_BLOCK = "hbm:concrete_colored_ext__2";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			context.runOnClient(client -> client.options.ambientOcclusion().set(true));
			world.getServer().runCommand("gamemode creative @a");
			context.waitTicks(60);
			world.getServer().runCommand("give @a " + PRESS);
			world.getServer().runCommand("give @a " + THRUSTER);
			world.getServer().runCommand("give @a " + COMPOSITE_BOBBLEHEAD);
			world.getServer().runCommand("give @a " + ORDINARY_BLOCK);
			world.getServer().runCommand("give @a " + RBMK_BLOCK);
			world.getServer().runCommand("give @a " + C4_BLOCK);
			world.getServer().runCommand("give @a " + LIGHTSTONE_BLOCK);
			world.getServer().runCommand("give @a " + CONCRETE_BLOCK);
			context.waitTicks(20);

			select(context, 0);
			context.takeScreenshot("obj_press_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("obj_press_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 1);
			context.takeScreenshot("obj_thruster_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("obj_thruster_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 2);
			context.takeScreenshot("obj_bobblehead_composite_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("obj_bobblehead_composite_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 3);
			context.takeScreenshot("block_steel_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("block_steel_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 4);
			context.takeScreenshot("rbmk_absorber_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("rbmk_absorber_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 5);
			context.takeScreenshot("c4_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("c4_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 6);
			context.takeScreenshot("lightstone_chiseled_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("lightstone_chiseled_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			select(context, 7);
			context.takeScreenshot("concrete_indigo_first_person");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);
			context.takeScreenshot("concrete_indigo_inventory");
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitTicks(20);

			world.getServer().runCommand("execute at @a run setblock ^-1 ^ ^8 hbm:charger replace");
			world.getServer().runCommand("execute at @a run setblock ^ ^ ^8 hbm:furnace_steel replace");
			world.getServer().runCommand("execute at @a run setblock ^1 ^ ^8 hbm:nuke_boy replace");
			world.getServer().runCommand("execute at @a run setblock ^-3 ^ ^12 hbm:machine_press[facing=north] replace");
			world.getServer().runCommand("execute at @a run setblock ^-1 ^ ^12 hbm:machine_press[facing=east] replace");
			world.getServer().runCommand("execute at @a run setblock ^1 ^ ^12 hbm:machine_press[facing=south] replace");
			world.getServer().runCommand("execute at @a run setblock ^3 ^ ^12 hbm:machine_press[facing=west] replace");
			context.waitTicks(40);
			context.takeScreenshot("special_blocks_placed");

			world.getServer().runCommand("fill ^-9 ^-1 ^-6 ^9 ^-1 ^10 minecraft:light_gray_concrete replace");
			world.getServer().runCommand("execute at @a run setblock ^-4 ^ ^3 hbm:block_steel replace");
			world.getServer().runCommand("execute at @a run setblock ^-2 ^ ^3 hbm:bobblehead__1 replace");
			world.getServer().runCommand("execute at @a run setblock ^2 ^ ^3 hbm:snowglobe__1 replace");
			context.waitTicks(40);
			context.takeScreenshot("trinkets_on_floor");

			world.getServer().runCommand("execute at @a run setblock ^-1 ^ ^6 hbm:machine_press[facing=north] replace");
			world.getServer().runCommand("execute at @a run setblock ^2 ^ ^6 hbm:machine_press[facing=east] replace");
			context.waitTicks(40);
			context.takeScreenshot("facing_test");
		}
	}

	private static void select(ClientGameTestContext context, int slot) {
		context.runOnClient(client -> {
			if (client.player != null) {
				client.player.getInventory().setSelectedSlot(slot);
			}
		});
		context.waitTicks(5);
	}
}

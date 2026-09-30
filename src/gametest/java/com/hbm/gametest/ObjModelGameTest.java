package com.hbm.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class ObjModelGameTest implements FabricClientGameTest {

	private static final String PRESS = "hbm:tile.machine_press[item_model=\"hbm:render/13749918a6cf7c3a674bdaadef5dac4b\"]";
	private static final String THRUSTER = "hbm:item.mp_thruster_10_kerosene[item_model=\"hbm:render/3eb3b9dd252bc85973e870690c0bc2b7__missile_parts_mp_t_10_kerosene__missile_parts_thrusters_mp_t_10_kerosene\"]";
	private static final String COMPOSITE_BOBBLEHEAD = "hbm:tile.bobblehead[item_model=\"hbm:render/00d616fc0f0ef5f193ad84ae05043cbd__composite_fd4981d39f4cbde66dd8\"]";
	private static final String ORDINARY_BLOCK = "hbm:block_steel";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			context.waitTicks(60);
			world.getServer().runCommand("give @a " + PRESS);
			world.getServer().runCommand("give @a " + THRUSTER);
			world.getServer().runCommand("give @a " + COMPOSITE_BOBBLEHEAD);
			world.getServer().runCommand("give @a " + ORDINARY_BLOCK);
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

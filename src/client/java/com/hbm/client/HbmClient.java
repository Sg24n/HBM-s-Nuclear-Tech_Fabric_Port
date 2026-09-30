package com.hbm.client;

import com.hbm.client.model.ObjUnbakedModel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.minecraft.resources.Identifier;

public class HbmClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(Identifier.fromNamespaceAndPath("hbm", "obj"), new ObjUnbakedModel.Deserializer());
	}
}

package com.hbm.component;

import com.hbm.main.MainRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class HbmDataComponents {

	public record IcfPellet(int type1, int type2, boolean muon) {
		public static final Codec<IcfPellet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("type1").forGetter(IcfPellet::type1),
				Codec.INT.fieldOf("type2").forGetter(IcfPellet::type2),
				Codec.BOOL.fieldOf("muon").forGetter(IcfPellet::muon)
		).apply(instance, IcfPellet::new));
	}

	public record Grenade(int filling, int shell, int fuze, int extra) {
		public static final Codec<Grenade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("filling").forGetter(Grenade::filling),
				Codec.INT.fieldOf("shell").forGetter(Grenade::shell),
				Codec.INT.fieldOf("fuze").forGetter(Grenade::fuze),
				Codec.INT.optionalFieldOf("extra", 0).forGetter(Grenade::extra)
		).apply(instance, Grenade::new));
	}

	public record BedrockOre(double nonmetal, double light, double crystal, double rare, double actinide, double heavy) {
		public static final Codec<BedrockOre> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.DOUBLE.fieldOf("nonmetal").forGetter(BedrockOre::nonmetal),
				Codec.DOUBLE.fieldOf("light").forGetter(BedrockOre::light),
				Codec.DOUBLE.fieldOf("crystal").forGetter(BedrockOre::crystal),
				Codec.DOUBLE.fieldOf("rare").forGetter(BedrockOre::rare),
				Codec.DOUBLE.fieldOf("actinide").forGetter(BedrockOre::actinide),
				Codec.DOUBLE.fieldOf("heavy").forGetter(BedrockOre::heavy)
		).apply(instance, BedrockOre::new));
	}

	public record MissileCustom(int chip, int fuselage, int warhead, int thruster, int stability, String name) {
		public static final Codec<MissileCustom> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("chip").forGetter(MissileCustom::chip),
				Codec.INT.fieldOf("fuselage").forGetter(MissileCustom::fuselage),
				Codec.INT.fieldOf("warhead").forGetter(MissileCustom::warhead),
				Codec.INT.fieldOf("thruster").forGetter(MissileCustom::thruster),
				Codec.INT.fieldOf("stability").forGetter(MissileCustom::stability),
				Codec.STRING.optionalFieldOf("name", "").forGetter(MissileCustom::name)
		).apply(instance, MissileCustom::new));
	}

	public static final DataComponentType<Long> CHARGE = DataComponentType.<Long>builder()
			.persistent(Codec.LONG).build();
	public static final DataComponentType<Integer> FLUID_ID = DataComponentType.<Integer>builder()
			.persistent(Codec.INT).build();
	public static final DataComponentType<Integer> CART_BASE = DataComponentType.<Integer>builder()
			.persistent(Codec.INT).build();
	public static final DataComponentType<String> BLUEPRINT = DataComponentType.<String>builder()
			.persistent(Codec.STRING).build();
	public static final DataComponentType<IcfPellet> ICF_PELLET = DataComponentType.<IcfPellet>builder()
			.persistent(IcfPellet.CODEC).build();
	public static final DataComponentType<Grenade> GRENADE = DataComponentType.<Grenade>builder()
			.persistent(Grenade.CODEC).build();
	public static final DataComponentType<BedrockOre> BEDROCK_ORE = DataComponentType.<BedrockOre>builder()
			.persistent(BedrockOre.CODEC).build();
	public static final DataComponentType<MissileCustom> MISSILE_CUSTOM = DataComponentType.<MissileCustom>builder()
			.persistent(MissileCustom.CODEC).build();

	private HbmDataComponents() {
	}

	public static void initialize() {
		register("charge", CHARGE);
		register("fluid_id", FLUID_ID);
		register("cart_base", CART_BASE);
		register("blueprint", BLUEPRINT);
		register("icf_pellet", ICF_PELLET);
		register("grenade", GRENADE);
		register("bedrock_ore", BEDROCK_ORE);
		register("missile_custom", MISSILE_CUSTOM);
	}

	private static <T> void register(String name, DataComponentType<T> type) {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
				Identifier.fromNamespaceAndPath(MainRegistry.MOD_ID, name), type);
	}
}

package com.hbm.material;

public class DictFrame {

	public final String[] mats;

	public DictFrame(String... mats) {
		this.mats = mats;
	}

	public String any() {
		return MaterialShapes.ANY.name() + mats[0];
	}

	public String nugget() {
		return MaterialShapes.NUGGET.name() + mats[0];
	}

	public String tiny() {
		return MaterialShapes.TINY.name() + mats[0];
	}

	public String bolt() {
		return MaterialShapes.BOLT.name() + mats[0];
	}

	public String ingot() {
		return MaterialShapes.INGOT.name() + mats[0];
	}

	public String dustTiny() {
		return MaterialShapes.DUSTTINY.name() + mats[0];
	}

	public String dust() {
		return MaterialShapes.DUST.name() + mats[0];
	}

	public String gem() {
		return MaterialShapes.GEM.name() + mats[0];
	}

	public String crystal() {
		return MaterialShapes.CRYSTAL.name() + mats[0];
	}

	public String plate() {
		return MaterialShapes.PLATE.name() + mats[0];
	}

	public String plateCast() {
		return MaterialShapes.CASTPLATE.name() + mats[0];
	}

	public String plateWelded() {
		return MaterialShapes.WELDEDPLATE.name() + mats[0];
	}

	public String wireFine() {
		return MaterialShapes.WIRE.name() + mats[0];
	}

	public String wireDense() {
		return MaterialShapes.DENSEWIRE.name() + mats[0];
	}

	public String shell() {
		return MaterialShapes.SHELL.name() + mats[0];
	}

	public String pipe() {
		return MaterialShapes.PIPE.name() + mats[0];
	}

	public String billet() {
		return MaterialShapes.BILLET.name() + mats[0];
	}

	public String block() {
		return MaterialShapes.BLOCK.name() + mats[0];
	}

	public String ore() {
		return MaterialShapes.ORE.name() + mats[0];
	}

	public String fragment() {
		return MaterialShapes.FRAGMENT.name() + mats[0];
	}

	public String lightBarrel() {
		return MaterialShapes.LIGHTBARREL.name() + mats[0];
	}

	public String heavyBarrel() {
		return MaterialShapes.HEAVYBARREL.name() + mats[0];
	}

	public String lightReceiver() {
		return MaterialShapes.LIGHTRECEIVER.name() + mats[0];
	}

	public String heavyReceiver() {
		return MaterialShapes.HEAVYRECEIVER.name() + mats[0];
	}

	public String mechanism() {
		return MaterialShapes.MECHANISM.name() + mats[0];
	}

	public String stock() {
		return MaterialShapes.STOCK.name() + mats[0];
	}

	public String grip() {
		return MaterialShapes.GRIP.name() + mats[0];
	}

	public String[] all(MaterialShapes shape) {
		return appendToAll(shape.prefixes);
	}

	private String[] appendToAll(String... prefix) {
		String[] names = new String[mats.length * prefix.length];
		for (int i = 0; i < mats.length; i++) {
			for (int j = 0; j < prefix.length; j++) {
				names[i * prefix.length + j] = prefix[j] + mats[i];
			}
		}
		return names;
	}
}

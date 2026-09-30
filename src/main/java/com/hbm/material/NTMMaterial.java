package com.hbm.material;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NTMMaterial {

	public final int id;
	public final String[] names;
	public final Set<MaterialShapes> autogen = new HashSet<>();
	public final Set<MatTraits> traits = new HashSet<>();
	public SmeltingBehavior smeltable = SmeltingBehavior.NOT_SMELTABLE;
	public int solidColorLight = 0xFF4A00;
	public int solidColorDark = 0x802000;
	public int moltenColor = 0xFF4A00;

	public NTMMaterial smeltsInto;
	public int convIn;
	public int convOut;

	public NTMMaterial(int id, DictFrame dict) {
		this.names = dict.mats;
		this.id = id;

		this.smeltsInto = this;
		this.convIn = 1;
		this.convOut = 1;

		for (String name : dict.mats) {
			Mats.matByName.put(name, this);
		}

		Mats.orderedList.add(this);
		Mats.matById.put(id, this);
	}

	public String translationKey() {
		return "hbm.material." + this.names[0].toLowerCase(Locale.ROOT);
	}

	public NTMMaterial setConversion(NTMMaterial mat, int in, int out) {
		this.smeltsInto = mat;
		this.convIn = in;
		this.convOut = out;
		return this;
	}

	public NTMMaterial setAutogen(MaterialShapes... shapes) {
		for (MaterialShapes shape : shapes) {
			this.autogen.add(shape);
		}
		return this;
	}

	public NTMMaterial setTraits(MatTraits... traits) {
		for (MatTraits trait : traits) {
			this.traits.add(trait);
		}
		return this;
	}

	public NTMMaterial m() {
		this.traits.add(MatTraits.METAL);
		return this;
	}

	public NTMMaterial n() {
		this.traits.add(MatTraits.NONMETAL);
		return this;
	}

	public NTMMaterial smeltable(SmeltingBehavior behavior) {
		this.smeltable = behavior;
		return this;
	}

	public NTMMaterial setSolidColor(int colorLight, int colorDark) {
		this.solidColorLight = colorLight;
		this.solidColorDark = colorDark;
		return this;
	}

	public NTMMaterial setMoltenColor(int color) {
		this.moltenColor = color;
		return this;
	}

	public enum SmeltingBehavior {
		NOT_SMELTABLE,
		VAPORIZES,
		BREAKS,
		SMELTABLE,
		ADDITIVE
	}

	public enum MatTraits {
		METAL,
		NONMETAL
	}
}

package com.hbm.test;

import com.hbm.material.Mats;
import com.hbm.material.MaterialShapes;
import com.hbm.material.NTMMaterial;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialMatrixTest {

	@Test
	void allMaterialsRegistered() {
		assertEquals(108, Mats.orderedList.size(), "material count");
		assertEquals(Mats.orderedList.size(), Mats.matById.size(), "distinct material ids");
	}

	@Test
	void idsAndNamesAreUnique() {
		Set<Integer> ids = new HashSet<>();
		for (NTMMaterial material : Mats.orderedList) {
			assertTrue(ids.add(material.id), "duplicate material id: " + material.id);
			for (String name : material.names) {
				assertSame(material, Mats.matByName.get(name), "name mapped to wrong material: " + name);
			}
		}
	}

	@Test
	void knownMaterialsResolve() {
		assertSame(Mats.MAT_IRON, Mats.matById.get(2600));
		assertSame(Mats.MAT_URANIUM, Mats.matByName.get("Uranium"));
		assertSame(Mats.MAT_SCHRABIDIUM, Mats.matByName.get("Schrabidium"));
		assertNotNull(Mats.MAT_IRON.smeltsInto);
	}

	@Test
	void autogenMatrixIsPopulated() {
		int pairs = 0;
		for (NTMMaterial material : Mats.orderedList) {
			for (MaterialShapes shape : material.autogen) {
				if (!shape.noAutogen) {
					pairs++;
				}
			}
		}
		assertTrue(pairs > 100, "autogen shape x material pairs: " + pairs);
		assertTrue(Mats.MAT_IRON.autogen.contains(MaterialShapes.DUST));
		assertTrue(Mats.MAT_IRON.autogen.contains(MaterialShapes.BLOCK));
	}

	@Test
	void shapesIndexTheirPrefixes() {
		assertSame(MaterialShapes.INGOT, MaterialShapes.byPrefix.get("ingot"));
		assertSame(MaterialShapes.WIRE, MaterialShapes.byPrefix.get("wireFine"));
		assertSame(MaterialShapes.TINY, MaterialShapes.byPrefix.get("tiny"));
		assertEquals("ingotUranium", MaterialShapes.INGOT.make(Mats.MAT_URANIUM));
	}
}

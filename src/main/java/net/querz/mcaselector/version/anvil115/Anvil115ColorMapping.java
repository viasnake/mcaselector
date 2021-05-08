package net.querz.mcaselector.version.anvil115;

import net.querz.mcaselector.debug.Debug;
import net.querz.mcaselector.text.TextHelper;
import net.querz.mcaselector.version.ColorMapping;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static net.querz.mcaselector.validation.ValidationHelper.withDefault;

public class Anvil115ColorMapping implements ColorMapping {

	//value can either be an Integer (color) or a BlockStateMapping
	private final Map<String, Object> mapping = new TreeMap<>();
	private final Set<String> grass = new HashSet<>();
	private final Set<String> foliage = new HashSet<>();

	public Anvil115ColorMapping() {
		// note_block:pitch=1,powered=true,instrument=flute;01ab9f
		// noinspection ConstantConditions
		try (BufferedReader bis = new BufferedReader(
				new InputStreamReader(Anvil115ColorMapping.class.getClassLoader().getResourceAsStream("mapping/115/colors.txt")))) {
			String line;
			while ((line = bis.readLine()) != null) {
				String[] elements = line.split(";");
				if (elements.length < 2 || elements.length > 3) {
					Debug.dumpf("invalid line in color file: \"%s\"", line);
					continue;
				}
				String[] blockData = elements[0].split(":");
				if (blockData.length > 2) {
					Debug.dumpf("invalid line in color file: \"%s\"", line);
					continue;
				}
				Integer color = TextHelper.parseInt(elements[1], 16);
				if (color == null || color < 0x0 || color > 0xFFFFFF) {
					Debug.dumpf("invalid color code in color file: \"%s\"", elements[1]);
				}

				if (blockData.length == 1) {
					//default block color, set value to Integer color
					mapping.put("minecraft:" + blockData[0], color);
				} else {
					BlockStateMapping bsm;
					if (mapping.containsKey("minecraft:" + blockData[0])) {
						bsm = (BlockStateMapping) mapping.get("minecraft:" + blockData[0]);
					} else {
						bsm = new BlockStateMapping();
						mapping.put("minecraft:" + blockData[0], bsm);
					}
					Set<String> conditions = new HashSet<>(Arrays.asList(blockData[1].split(",")));
					bsm.blockStateMapping.put(conditions, color);
				}
				if (elements.length == 3) {
					switch (elements[2]) {
						case "g":
							grass.add("minecraft:" + blockData[0]);
							break;
						case "f":
							foliage.add("minecraft:" + blockData[0]);
							break;
						default:
							throw new RuntimeException("invalid grass / foliage type " + elements[2]);
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("failed to read mapping/115/colors.txt");
		}
	}

	@Override
	public int getRGB(Object o, int biome) {
		String name = withDefault(() -> ((CompoundTag) o).getString("Name"), "");
		Object value = mapping.get(name);
		if (value instanceof Integer) {
			return applyBiomeTint(name, biome, (int) value);
		} else if (value instanceof BlockStateMapping) {
			int color = ((BlockStateMapping) value).getColor(withDefault(() -> ((CompoundTag) o).getCompoundTag("Properties"), null));
			return applyBiomeTint(name, biome, color);
		}
		return 0x000000;
	}

	private int applyBiomeTint(String name, int biome, int color) {
		if (grass.contains(name)) {
			return applyTint(color, biomeGrassTints[biome]);
		} else if (foliage.contains(name)) {
			return applyTint(color, biomeFoliageTints[biome]);
		} else if (name.equals("minecraft:water")) {
			return applyTint(color, biomeWaterTints[biome]);
		}
		return color;
	}

	private static class BlockStateMapping {

		private final Map<Set<String>, Integer> blockStateMapping = new HashMap<>();

		public int getColor(CompoundTag properties) {
			if (properties != null) {
				for (Map.Entry<String, Tag<?>> property : properties.entrySet()) {
					Map<Set<String>, Integer> clone = new HashMap<>(blockStateMapping);
					for (Map.Entry<Set<String>, Integer> blockState : blockStateMapping.entrySet()) {
						String value = property.getKey() + "=" + ((StringTag) property.getValue()).getValue();
						if (!blockState.getKey().contains(value)) {
							clone.remove(blockState.getKey());
						}
					}
					Iterator<Map.Entry<Set<String>, Integer>> it = clone.entrySet().iterator();
					if (it.hasNext()) {
						return it.next().getValue();
					}
				}
			}
			return 0x000000;
		}
	}
}

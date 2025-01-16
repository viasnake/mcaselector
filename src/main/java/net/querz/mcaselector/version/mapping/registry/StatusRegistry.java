package net.querz.mcaselector.version.mapping.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StatusRegistry {

	private static final Logger LOGGER = LogManager.getLogger(StatusRegistry.class);

	private StatusRegistry() {}

	private static final Map<String, String> valid = new HashMap<>();

	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	static {
		try (BufferedReader bis = new BufferedReader(new InputStreamReader(
				Objects.requireNonNull(StatusRegistry.class.getClassLoader().getResourceAsStream("mapping/registry/status.json"))))) {
			List<String> status = GSON.fromJson(bis, new TypeToken<List<String>>(){}.getType());
			for (String s : status) {
				valid.put(s, "minecraft:" + s);
				valid.put("minecraft:" + s, s);
			}
		} catch (IOException ex) {
			LOGGER.error("error reading mapping/registry/status.json", ex);
		}
	}

	public static boolean isValidName(String name) {
		return valid.containsKey(name) || name != null && name.startsWith("'") && name.endsWith("'");
	}

	public static class StatusIdentifier {
		String name;
		String nameWithNamespace;
		boolean custom = false;

		public StatusIdentifier(String name) {
			if (name != null && name.startsWith("'") && name.endsWith("'")) {
				this.name = name.substring(1, name.length() - 1);
				custom = true;
			} else if (name != null && isValidName(name)) {
				initValid(name);
			} else {
				throw new IllegalArgumentException("invalid status");
			}
		}

		public StatusIdentifier(String name, boolean custom) {
			if (custom) {
				this.name = name;
				this.custom = true;
			} else if (isValidName(name)) {
				initValid(name);
			} else {
				throw new IllegalArgumentException("invalid status");
			}
		}

		private void initValid(String name) {
			if (name.startsWith("minecraft:")) {
				this.name = valid.get(name);
				this.nameWithNamespace = name;
			} else {
				this.name = name;
				this.nameWithNamespace = valid.get(name);
			}
		}

		public String getStatus() {
			return name;
		}

		public String getStatusWithNamespace() {
			if (custom) {
				return name;
			}
			return nameWithNamespace;
		}

		public boolean equals(String value) {
			if (value == null) {
				return false;
			}
			if (custom) {
				return name.equals(value);
			}
			return value.startsWith("minecraft:") && value.equals(nameWithNamespace) || value.equals(name);
		}

		@Override
		public String toString() {
			return name;
		}
	}
}

package net.querz.mcaselector.tiles.overlay;

import net.querz.mcaselector.io.mca.ChunkData;
import net.querz.mcaselector.text.TextHelper;

public class TimestampParser extends OverlayParser {

	private String minTimestamp;
	private String maxTimestamp;

	public TimestampParser() {
		super(OverlayType.TIMESTAMP);
	}

	@Override
	public int parseValue(ChunkData chunkData) {
		if (chunkData.getRegion() == null) {
			return 0;
		}
		return chunkData.getRegion().getTimestamp();
	}

	@Override
	public String name() {
		return "Timestamp";
	}

	public String minString() {
		return minTimestamp == null ? super.minString() : minTimestamp;
	}

	public String maxString() {
		return minTimestamp == null ? super.maxString() : maxTimestamp;
	}

	@Override
	public boolean setMin(String raw) {
		setRawMin(raw);
		minTimestamp = null;
		if (raw == null || raw.isEmpty()) {
			return setMin((Integer) null);
		}
		try {
			return setMin(Integer.parseInt(raw));
		} catch (NumberFormatException ex) {
			try {
				int timestamp = TextHelper.parseTimestamp(raw);
				boolean res = setMin(timestamp);
				if (res) {
					minTimestamp = raw;
				}
				return res;
			} catch (IllegalArgumentException ex2) {
				return setMin((Integer) null);
			}
		}
	}

	@Override
	public boolean setMax(String raw) {
		setRawMax(raw);
		maxTimestamp = null;
		if (raw == null || raw.isEmpty()) {
			return setMax((Integer) null);
		}
		try {
			return setMax(Integer.parseInt(raw));
		} catch (NumberFormatException ex) {
			try {
				int timestamp = TextHelper.parseTimestamp(raw);
				boolean res = setMax(timestamp);
				if (res) {
					maxTimestamp = raw;
				}
				return res;
			} catch (IllegalArgumentException ex2) {
				return setMax((Integer) null);
			}
		}
	}
}

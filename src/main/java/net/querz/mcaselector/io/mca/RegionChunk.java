package net.querz.mcaselector.io.mca;

import net.querz.mcaselector.io.FileHelper;
import net.querz.mcaselector.util.point.Point2i;
import net.querz.mcaselector.util.point.Point3i;
import net.querz.mcaselector.util.range.Range;
import net.querz.mcaselector.version.ChunkFilter;
import net.querz.mcaselector.version.Helper;
import net.querz.mcaselector.version.VersionHandler;
import net.querz.nbt.CompoundTag;
import java.io.File;
import java.util.List;

public class RegionChunk extends Chunk implements Cloneable {

	public RegionChunk(Point2i absoluteLocation) {
		super(absoluteLocation);
	}

	@Override
	public boolean relocate(Point3i offset) {
		return VersionHandler.getImpl(Helper.getDataVersion(data), ChunkFilter.Relocate.class).relocate(data, offset);
	}

	@Override
	public void merge(CompoundTag destination, List<Range> ranges, int yOffset) {
		VersionHandler.getImpl(Helper.getDataVersion(data), ChunkFilter.Merge.class).mergeChunks(data, destination, ranges, yOffset);
	}

	@Override
	public File getMCCFile() {
		return FileHelper.createRegionMCCFilePath(absoluteLocation);
	}

	@Override
	public RegionChunk clone() {
		return clone(RegionChunk::new);
	}
}

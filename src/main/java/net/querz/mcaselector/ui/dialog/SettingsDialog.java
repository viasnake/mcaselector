package net.querz.mcaselector.ui.dialog;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import net.querz.mcaselector.Config;
import net.querz.mcaselector.text.Translation;
import net.querz.mcaselector.ui.FileTextField;
import net.querz.mcaselector.ui.TileMapBox;
import net.querz.mcaselector.ui.UIFactory;
import java.io.File;
import java.util.*;

public class SettingsDialog extends Dialog<SettingsDialog.Result> {

	/*
	* Region selection color and opacity
	* Chunk selection color and opacity
	* MCAFilePipe thread options:
	* - Number of threads for file reading
	* - Number of threads for processing
	* - Number of threads for writing
	* - Maximum amount of loaded files
	* toggle shading
	* toggle shading of water
	* toggle showing non-existent regions
	* toggle smooth rendering
	* toggle smooth overlays
	* background pattern
	* minecraft saves folder
	* toggle debug
	* */

	private static final int processorCount = Runtime.getRuntime().availableProcessors();
	private static final long maxMemory = Runtime.getRuntime().maxMemory();

	private final ComboBox<Locale> languages = new ComboBox<>();

	private final Slider readThreadsSlider = createSlider(1, processorCount, 1, Config.getLoadThreads());
	private final Slider processThreadsSlider = createSlider(1, processorCount * 2, 1, Config.getProcessThreads());
	private final Slider writeThreadsSlider = createSlider(1, processorCount, 1, Config.getWriteThreads());
	private final Slider maxLoadedFilesSlider = createSlider(1, (int) Math.max(Math.ceil(maxMemory / 1_000_000_000D) * 6, 4), 1, Config.getMaxLoadedFiles());
	private final Button regionSelectionColorPreview = new Button();
	private final Button chunkSelectionColorPreview = new Button();
	private final Button pasteChunksColorPreview = new Button();
	private final CheckBox shadeCheckBox = new CheckBox();
	private final CheckBox shadeWaterCheckBox = new CheckBox();
	private final CheckBox showNonexistentRegionsCheckBox = new CheckBox();
	private final CheckBox smoothRendering = new CheckBox();
	private final CheckBox smoothOverlays = new CheckBox();
	private final ComboBox<TileMapBox.TileMapBoxBackground> tileMapBackgrounds = new ComboBox<>();
	private final FileTextField mcSavesDir = new FileTextField();
	private final CheckBox debugCheckBox = new CheckBox();

	private Color regionSelectionColor = Config.getRegionSelectionColor().makeJavaFXColor();
	private Color chunkSelectionColor = Config.getChunkSelectionColor().makeJavaFXColor();
	private Color pasteChunksColor = Config.getPasteChunksColor().makeJavaFXColor();

	private final ButtonType reset = new ButtonType(Translation.DIALOG_SETTINGS_RESET.toString(), ButtonBar.ButtonData.LEFT);

	public SettingsDialog(Stage primaryStage) {
		titleProperty().bind(Translation.DIALOG_SETTINGS_TITLE.getProperty());
		initStyle(StageStyle.UTILITY);
		getDialogPane().getStyleClass().add("settings-dialog-pane");
		getDialogPane().getScene().getStylesheets().addAll(primaryStage.getScene().getStylesheets());
		getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, reset);

		getDialogPane().lookupButton(reset).addEventFilter(ActionEvent.ACTION, e -> {
			e.consume();
			languages.setValue(Config.DEFAULT_LOCALE);
			readThreadsSlider.setValue(Config.DEFAULT_LOAD_THREADS);
			processThreadsSlider.setValue(Config.DEFAULT_PROCESS_THREADS);
			writeThreadsSlider.setValue(Config.DEFAULT_WRITE_THREADS);
			maxLoadedFilesSlider.setValue(Config.DEFAULT_MAX_LOADED_FILES);
			regionSelectionColor = Config.DEFAULT_REGION_SELECTION_COLOR.makeJavaFXColor();
			regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_REGION_SELECTION_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			chunkSelectionColor = Config.DEFAULT_CHUNK_SELECTION_COLOR.makeJavaFXColor();
			chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_CHUNK_SELECTION_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			pasteChunksColor = Config.DEFAULT_PASTE_CHUNKS_COLOR.makeJavaFXColor();
			pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(Config.DEFAULT_PASTE_CHUNKS_COLOR.makeJavaFXColor(), CornerRadii.EMPTY, Insets.EMPTY)));
			shadeCheckBox.setSelected(Config.DEFAULT_SHADE);
			shadeWaterCheckBox.setSelected(Config.DEFAULT_SHADE_WATER);
			showNonexistentRegionsCheckBox.setSelected(Config.DEFAULT_SHOW_NONEXISTENT_REGIONS);
			smoothRendering.setSelected(Config.DEFAULT_SMOOTH_RENDERING);
			smoothOverlays.setSelected(Config.DEFAULT_SMOOTH_OVERLAYS);
			tileMapBackgrounds.setValue(TileMapBox.TileMapBoxBackground.valueOf(Config.DEFAULT_TILEMAP_BACKGROUND));
			mcSavesDir.setFile(Config.DEFAULT_MC_SAVES_DIR == null ? null : new File(Config.DEFAULT_MC_SAVES_DIR));
			debugCheckBox.setSelected(Config.DEFAULT_DEBUG);
		});

		setResultConverter(c -> {
			if (c.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
				return new Result(
						languages.getSelectionModel().getSelectedItem(),
						(int) readThreadsSlider.getValue(),
						(int) processThreadsSlider.getValue(),
						(int) writeThreadsSlider.getValue(),
						(int) maxLoadedFilesSlider.getValue(),
						regionSelectionColor,
						chunkSelectionColor,
						pasteChunksColor,
						shadeCheckBox.isSelected(),
						shadeWaterCheckBox.isSelected(),
						showNonexistentRegionsCheckBox.isSelected(),
						smoothRendering.isSelected(),
						smoothOverlays.isSelected(),
						tileMapBackgrounds.getSelectionModel().getSelectedItem(),
						mcSavesDir.getFile(),
						debugCheckBox.isSelected()
				);
			}
			return null;
		});

		languages.getItems().addAll(Translation.getAvailableLanguages());
		languages.setValue(Config.getLocale());
		languages.setConverter(new StringConverter<>() {

			final Map<String, Locale> cache = new HashMap<>();

			@Override
			public String toString(Locale locale) {
				String display = locale.getDisplayName(locale);
				cache.put(display, locale);
				return display;
			}

			@Override
			public Locale fromString(String string) {
				return cache.get(string);
			}
		});
		languages.getStyleClass().add("languages-combo-box");

		regionSelectionColorPreview.getStyleClass().clear();
		chunkSelectionColorPreview.getStyleClass().clear();
		pasteChunksColorPreview.getStyleClass().clear();
		regionSelectionColorPreview.getStyleClass().add("color-preview-button");
		chunkSelectionColorPreview.getStyleClass().add("color-preview-button");
		pasteChunksColorPreview.getStyleClass().add("color-preview-button");
		regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(regionSelectionColor, CornerRadii.EMPTY, Insets.EMPTY)));
		chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(chunkSelectionColor, CornerRadii.EMPTY, Insets.EMPTY)));
		pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(pasteChunksColor, CornerRadii.EMPTY, Insets.EMPTY)));
		shadeCheckBox.setSelected(Config.shade());
		shadeWaterCheckBox.setSelected(Config.shadeWater());
		showNonexistentRegionsCheckBox.setSelected(Config.showNonExistentRegions());
		smoothRendering.setSelected(Config.smoothRendering());
		smoothOverlays.setSelected(Config.smoothOverlays());
		tileMapBackgrounds.getItems().addAll(TileMapBox.TileMapBoxBackground.values());

		tileMapBackgrounds.setCellFactory((listView) -> {
			ListCell<TileMapBox.TileMapBoxBackground> cell = new ListCell<>() {

				@Override
				public void updateIndex(int i) {
					super.updateIndex(i);
					TileMapBox.TileMapBoxBackground[] values = TileMapBox.TileMapBoxBackground.values();
					if (i < 0 || i >= values.length) {
						return;
					}
					setBackground(values[i].getBackground());
				}
			};
			// we don't want this to be treated like a regular list cell
			cell.getStyleClass().clear();
			return cell;
		});
		tileMapBackgrounds.setButtonCell(tileMapBackgrounds.getCellFactory().call(null));
		tileMapBackgrounds.getStyleClass().add("tilemap-backgrounds-combo-box");

		tileMapBackgrounds.setValue(TileMapBox.TileMapBoxBackground.valueOf(Config.getTileMapBackground()));
		mcSavesDir.setFile(Config.getMCSavesDir() == null ? null : new File(Config.getMCSavesDir()));
		debugCheckBox.setSelected(Config.debug());

		regionSelectionColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), regionSelectionColor).showColorPicker();
			result.ifPresent(c -> {
				regionSelectionColor = c;
				regionSelectionColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});
		chunkSelectionColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), chunkSelectionColor).showColorPicker();
			result.ifPresent(c -> {
				chunkSelectionColor = c;
				chunkSelectionColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});
		pasteChunksColorPreview.setOnMousePressed(e -> {
			Optional<Color> result = new ColorPicker(getDialogPane().getScene().getWindow(), pasteChunksColor).showColorPicker();
			result.ifPresent(c -> {
				pasteChunksColor = c;
				pasteChunksColorPreview.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
			});
		});

		shadeCheckBox.setOnAction(e -> shadeWaterCheckBox.setDisable(!shadeCheckBox.isSelected()));
		shadeWaterCheckBox.setDisable(!shadeCheckBox.isSelected());

		HBox debugBox = new HBox();
		debugBox.getStyleClass().add("debug-box");
		Hyperlink logFileLink = UIFactory.explorerLink(Translation.DIALOG_SETTINGS_SHOW_LOG_FILE, Config.getLogFile().getParentFile(), null);
		debugBox.getChildren().addAll(debugCheckBox, logFileLink);

		GridPane grid = new GridPane();
		grid.getStyleClass().add("slider-grid-pane");
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_LANGUAGE), 0, 0, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_READ_THREADS), 0, 1, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_PROCESS_THREADS), 0, 2, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_WRITE_THREADS), 0, 3, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_MAX_FILES), 0, 4, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_REGION_COLOR), 0, 5, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_CHUNK_COLOR), 0, 6, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_PASTED_CHUNKS_COLOR), 0, 7, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_SHADE), 0, 8, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_SHADE_WATER), 0, 9, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_SHOW_NONEXISTENT_REGIONS), 0, 10, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_SMOOTH_RENDERING), 0, 11, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_SMOOTH_OVERLAYS), 0, 12, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_BACKGROUND_PATTERN), 0, 13, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_MC_SAVES_DIR), 0, 14, 1, 1);
		grid.add(UIFactory.label(Translation.DIALOG_SETTINGS_PRINT_DEBUG), 0, 15, 1, 1);
		grid.add(languages, 1, 0, 2, 1);
		grid.add(readThreadsSlider, 1, 1, 1, 1);
		grid.add(processThreadsSlider, 1, 2, 1, 1);
		grid.add(writeThreadsSlider, 1, 3, 1, 1);
		grid.add(maxLoadedFilesSlider, 1, 4, 1, 1);
		grid.add(regionSelectionColorPreview, 1, 5, 2, 1);
		grid.add(chunkSelectionColorPreview, 1, 6, 2, 1);
		grid.add(pasteChunksColorPreview, 1, 7, 2, 1);
		grid.add(shadeCheckBox, 1, 8, 2, 1);
		grid.add(shadeWaterCheckBox, 1, 9, 2, 1);
		grid.add(showNonexistentRegionsCheckBox, 1, 10, 2, 1);
		grid.add(smoothRendering, 1, 11, 2, 1);
		grid.add(smoothOverlays, 1, 12, 2, 1);
		grid.add(tileMapBackgrounds, 1, 13, 2, 1);
		grid.add(mcSavesDir, 1, 14, 2, 1);
		grid.add(debugBox, 1, 15, 2, 1);
		grid.add(UIFactory.attachTextFieldToSlider(readThreadsSlider), 2, 1, 1, 1);
		grid.add(UIFactory.attachTextFieldToSlider(processThreadsSlider), 2, 2, 1, 1);
		grid.add(UIFactory.attachTextFieldToSlider(writeThreadsSlider), 2, 3, 1, 1);
		grid.add(UIFactory.attachTextFieldToSlider(maxLoadedFilesSlider), 2, 4, 1, 1);

		getDialogPane().setContent(grid);
	}

	private Slider createSlider(int min, int max, int steps, int init) {
		Slider slider = new Slider(min, max, init);
		int majorTicks = (int) (Math.ceil(max - min) / 5);
		slider.setMajorTickUnit(majorTicks);
		slider.setMinorTickCount(majorTicks - 1);
		slider.setBlockIncrement(steps);
		return slider;
	}

	public static class Result {

		private final int readThreads, processThreads, writeThreads, maxLoadedFiles;
		private final Color regionColor, chunkColor, pasteColor;
		private final boolean shadeWater;
		private final boolean shade;
		private final boolean showNonexistentRegions;
		private final boolean smoothRendering, smoothOverlays;
		private final TileMapBox.TileMapBoxBackground tileMapBackground;
		private final File mcSavesDir;
		private final boolean debug;
		private final Locale locale;

		public Result(Locale locale, int readThreads, int processThreads, int writeThreads, int maxLoadedFiles,
		              Color regionColor, Color chunkColor, Color pasteColor, boolean shade, boolean shadeWater,
		              boolean showNonexistentRegions, boolean smoothRendering, boolean smoothOverlays,
		              TileMapBox.TileMapBoxBackground tileMapBackground, File mcSavesDir, boolean debug) {

			this.locale = locale;
			this.readThreads = readThreads;
			this.processThreads = processThreads;
			this.writeThreads = writeThreads;
			this.maxLoadedFiles = maxLoadedFiles;
			this.regionColor = regionColor;
			this.chunkColor = chunkColor;
			this.pasteColor = pasteColor;
			this.shade = shade;
			this.shadeWater = shadeWater;
			this.showNonexistentRegions = showNonexistentRegions;
			this.smoothRendering = smoothRendering;
			this.smoothOverlays = smoothOverlays;
			this.tileMapBackground = tileMapBackground;
			this.mcSavesDir = Objects.requireNonNullElseGet(mcSavesDir, () -> new File(Config.DEFAULT_MC_SAVES_DIR));
			this.debug = debug;
		}

		public Locale getLocale() {
			return locale;
		}

		public int getReadThreads() {
			return readThreads;
		}

		public int getProcessThreads() {
			return processThreads;
		}

		public int getWriteThreads() {
			return writeThreads;
		}

		public int getMaxLoadedFiles() {
			return maxLoadedFiles;
		}

		public Color getRegionColor() {
			return regionColor;
		}

		public Color getChunkColor() {
			return chunkColor;
		}

		public Color getPasteColor() {
			return pasteColor;
		}

		public boolean getShade() {
			return shade;
		}

		public boolean getShadeWater() {
			return shadeWater;
		}

		public boolean getShowNonexistentRegions() {
			return showNonexistentRegions;
		}

		public boolean getSmoothRendering() {
			return smoothRendering;
		}

		public boolean getSmoothOverlays() {
			return smoothOverlays;
		}

		public TileMapBox.TileMapBoxBackground getTileMapBackground() {
			return tileMapBackground;
		}

		public File getMcSavesDir() {
			return mcSavesDir;
		}

		public boolean getDebug() {
			return debug;
		}
	}
}

package org.gmc.ssseditor;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.gmc.ssseditor.qualities.Companion;
import org.gmc.ssseditor.qualities.Good;
import org.gmc.ssseditor.qualities.ItemTags;
import org.gmc.ssseditor.qualities.QualitiesList;
import org.gmc.ssseditor.qualities.QualityItem;
import org.gmc.ssseditor.qualities.Ship;
import org.gmc.ssseditor.ui.QualityItemUI;
import org.gmc.ssseditor.ui.QualityItemUI.IQualityItemUIEvents;
import org.gmc.ssseditor.ui.SSSaveEditorUI;

import com.owlike.genson.Genson;

public class SSSaveEditor implements SSSaveEditorUI.ISaveEditorEvents, IQualityItemUIEvents {

	private SSSaveEditorUI ui;
	private File openFile = null;
	private HashMap<String, Object> data;
	private Genson jsonLib = new Genson();
	
	private boolean showUnknown = false;

	private Map<String, Object> echosQuality = null;
	private Map<String, Object> fuelQuality = null;
	private Map<String, Object> suppliesQuality = null;
	private Map<String, Object> terrorQuality = null;
	private Map<String, Object> hullQuality = null;
	private Map<String, Object> mirrorsQuality = null;
	private Map<String, Object> ironQuality = null;
	private Map<String, Object> pagesQuality = null;
	private Map<String, Object> heartsQuality = null;
	private Map<String, Object> veilsQuality = null;
	private Map<String, Object> crewQuality = null;

	/**
	 * The current player ship.
	 */
	private Ship ship;

	private QualitiesList saveFileQualities;
	private QualitiesList configurationQualities;

	/**
	 * This links save file Qualities in the tabs to the associated input field; it's used
	 * to update the Qualities when the save file is written out.
	 */
	private Map<Map<String, Object>, QualityItemUI> qualitiesFieldsMap = new HashMap<Map<String, Object>, QualityItemUI>();

	/**
	 * Total quantity of cargo.
	 */
	private long cargoQuantity;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try {
					SSSaveEditor window = new SSSaveEditor();
					window.ui.setVisible();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SSSaveEditor()
	{
		ui = new SSSaveEditorUI();
		ui.initialize();
		ui.addEventHandler(this);
		loadConfiguration();
	}

	/**
	 * Save. Invoked by File menu, Save entry.
	 */
	public void onFileSave()
	{
		this.saveTo(this.openFile, false);
	}
	
	/**
	 * Save As. Invoked by File menu, Save As entry.
	 */
	public void onFileSaveAs()
	{
		JFileChooser open = createFileChooser();
		int status = open.showSaveDialog(this.ui.frame);
		if (status == JFileChooser.APPROVE_OPTION) {
			this.saveTo(open.getSelectedFile(), true);
		}
		
	}

	/**
	 * Save to a file.
	 *
	 * After successfully saving the file, this.openFile is set to the saved file, and the
	 * title label is updated with the saved file's name.
	 *
	 * @param saveFile Target file. If the file's name does not end with ".json", it is added.
	 * @param confirm If true, confirm overwrite before proceeding.
	 */
	private void saveTo(File saveFile, boolean confirm)
	{
		try {
			// If the file doesn't have an extension, add ".json".
			String name = saveFile.getName();
			if (name.indexOf('.') == 1) {
				saveFile = new File(saveFile.getPath() + ".json");
			}
	
			if (this.ship != null && Integer.parseInt(this.ui.hullField.getValue()) > this.ship.getMaxHull()) {
				if (!this.ui.displayWarningDialog("Hull value " + this.ui.hullField.getValue() + "is above maximum, continue?", "Check Hull")) {
					return;
				}
			}
	
			// Update JSON.
			this.setQualityFromUI(this.echosQuality, this.ui.echosField);
			this.setQualityFromUI(this.fuelQuality, this.ui.fuelField);
			this.setQualityFromUI(this.suppliesQuality, this.ui.suppliesField);
			this.setQualityFromUI(this.terrorQuality, this.ui.terrorField);
			this.setQualityFromUI(this.hullQuality, this.ui.hullField);
			this.setQualityFromUI(this.mirrorsQuality, this.ui.mirrorsField);
			this.setQualityFromUI(this.ironQuality, this.ui.ironField);
			this.setQualityFromUI(this.pagesQuality, this.ui.pagesField);
			this.setQualityFromUI(this.heartsQuality, this.ui.heartsField);
			this.setQualityFromUI(this.veilsQuality, this.ui.veilsField);
			this.setQualityFromUI(this.crewQuality, this.ui.crewField);
	
			// Set items from generic list.
			for (Entry<Map<String, Object>, QualityItemUI> entry : this.qualitiesFieldsMap.entrySet()) {
				this.setQualityFromUI(entry.getKey(), entry.getValue());
			}
	
			if (saveFile.exists())
			{
				if (confirm) {
					if (!this.ui.displayWarningDialog("Overwrite " + saveFile.getName() + "?", "Overwrite")) {
						return;
					}
				}
				File backup = new File(saveFile.getPath() + ".bak");
				backup.delete();
				saveFile.renameTo(backup);
			}
	
			try (OutputStream output = new FileOutputStream(saveFile)) {
				this.jsonLib.serialize(this.data, output);
				this.ui.displayMessageDialog("Saved to " + saveFile.getName(), "Saved");
				// Update current file and title text.
				this.openFile = saveFile;
				this.ui.setTitle(saveFile.getName());
			} catch (FileNotFoundException e) {
				this.ui.displayErrorDialog("When writing, file not found (possibly missing directory)", "Error");
			} catch (IOException e) {
				this.ui.displayErrorDialog("Error writing file", "Error");
			}
		} catch (NumberFormatException e) {
			this.ui.displayErrorDialog("There was some input field with a non-numeric string in it; can't save.", "Error");
		}
	}

	/**
	 * Open a file. Invoked from the File menu, Open item.
	 */
	public void onFileOpen()
	{
		JFileChooser open = createFileChooser();
		int status = open.showOpenDialog(this.ui.frame);
		if (status == JFileChooser.APPROVE_OPTION) {
			if (!open.getSelectedFile().exists() || !open.getSelectedFile().canRead() || !open.getSelectedFile().canWrite()) {
				this.ui.displayErrorDialog("File does not exist or is not read/write", "Error");
				return;
			}

			this.openFile = open.getSelectedFile();
			loadFile(this.openFile);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFile(File source)
	{
		try (InputStream input = new FileInputStream(source)) {
			this.data = jsonLib.deserialize(input, HashMap.class);
			// Find significant data in the save.
			this.setUIFromKey(this.ui.playerLabel, this.data, "Name");
			this.setUIFromKey(this.ui.currentPortLabel, this.data, "CurrentPort", "Name");
			String inGameDate = this.data.get("InGameDate").toString();
			DateFormat isoDateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Date when = isoDateParser.parse(inGameDate);
			this.ui.inGameDateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT).format(when));
			
			// QualitiesPossesedList.
			Object qualities = this.data.get("QualitiesPossessedList");
			if (qualities instanceof ArrayList<?>) {
				this.importQualities((ArrayList<Object>) qualities);
			}
			
			this.ui.setTitle(source.getName());
			this.ui.setWindowTitleSuffix(source.getName());

			this.ui.saveMenuItem.setEnabled(true);
			this.ui.saveAsMenuItem.setEnabled(true);
			
		} catch (FileNotFoundException e) {
			this.ui.displayErrorDialog("File not found", "Error");
		} catch (IOException e) {
			this.ui.displayErrorDialog("Error reading file", "Error");
		} catch (ParseException e) {
			this.ui.displayErrorDialog("Error parsing file", "Error");
		}
	}

	/**
	 * Create a JFileChooser set to open Sunless Sea saves (.json) from the save directory.
	 * @return A JFileChooser.
	 */
	private JFileChooser createFileChooser() {
		JFileChooser open = new JFileChooser();
		open.setFileFilter(new FileNameExtensionFilter("Sunless Sea Saves", "json"));
		open.setAcceptAllFileFilterUsed(true);

		String saveDirectory = this.getSunlessSeaDirectory("saves");
		if (saveDirectory != null) {
			open.setCurrentDirectory(new File(saveDirectory));
		}

		return open;
	}

	/**
	 * Import qualities from the list. This imports known qualities and saves them in the class.
	 * @param qualities The qualities list.
	 */
	@SuppressWarnings("unchecked")
	private void importQualities(ArrayList<Object> qualities)
	{
		// Pre-defined qualities.
		this.echosQuality = this.setUIFromQuality(this.ui.echosField, qualities, ItemTags.echos);
		this.fuelQuality = this.setUIFromQuality(this.ui.fuelField, qualities, ItemTags.fuel);
		this.suppliesQuality = this.setUIFromQuality(this.ui.suppliesField, qualities, ItemTags.supplies);
		this.terrorQuality = this.setUIFromQuality(this.ui.terrorField, qualities, ItemTags.terror);
		this.hullQuality = this.setUIFromQuality(this.ui.hullField, qualities, ItemTags.hull);
		this.mirrorsQuality = this.setUIFromQuality(this.ui.mirrorsField, qualities, ItemTags.mirrors);
		this.ironQuality = this.setUIFromQuality(this.ui.ironField, qualities, ItemTags.iron);
		this.pagesQuality = this.setUIFromQuality(this.ui.pagesField, qualities, ItemTags.pages);
		this.heartsQuality = this.setUIFromQuality(this.ui.heartsField, qualities, ItemTags.hearts);
		this.veilsQuality = this.setUIFromQuality(this.ui.veilsField, qualities, ItemTags.veils);
		this.crewQuality = this.setUIFromQuality(this.ui.crewField, qualities, ItemTags.crew);
		
		Set<Long> displayedQualities = new HashSet<Long>();
		displayedQualities.add(ItemTags.echos.getTag());
		displayedQualities.add(ItemTags.fuel.getTag());
		displayedQualities.add(ItemTags.supplies.getTag());
		displayedQualities.add(ItemTags.terror.getTag());
		displayedQualities.add(ItemTags.hull.getTag());
		displayedQualities.add(ItemTags.mirrors.getTag());
		displayedQualities.add(ItemTags.iron.getTag());
		displayedQualities.add(ItemTags.pages.getTag());
		displayedQualities.add(ItemTags.hearts.getTag());
		displayedQualities.add(ItemTags.veils.getTag());
		displayedQualities.add(ItemTags.crew.getTag());

		this.ui.setShipName("No ship?");
		this.ui.setShipCrewCapacity(0);
		this.ui.setShipCargoCapacity(0);
		this.ui.setShipMaxHull(0);
		this.ui.setShipWeight(0);
		this.ship = null;
		this.ui.cookLabel.setText("");
		this.ui.foLabel.setText("");
		this.ui.engineerLabel.setText("");
		this.ui.gunneryLabel.setText("");
		this.ui.mascotLabel.setText("");
		this.ui.surgeonLabel.setText("");

		// Dump other qualities.
		this.ui.clearPanels();
		this.qualitiesFieldsMap.clear();
		this.saveFileQualities = new QualitiesList(qualities, false);
		
		// Find the Ship. This will be an entry in the Qualities with an AssociatedQualityId=102889 {Curiosity,CurrentShip}.
		for (Object item : qualities) {
			if (item instanceof Map<?,?>) {
				Map<String, Object> quality = (Map<String, Object>) item;
				if (JSONUtil.getLong(quality, "AssociatedQualityId") == 102889) {
					// Need to find the EquippedPossession/AssociatedQualityId to find the Ship definition.
					long shipId = JSONUtil.getLong(quality,  "EquippedPossession", "AssociatedQualityId");
					Ship shipBase = null; 
					List<QualityItem> ships = this.configurationQualities.GetQualities(Ship.categoryName);
					for (QualityItem shipCandidate : ships) {
						if (shipCandidate.getTag() == shipId) {
							shipBase = (Ship) shipCandidate;
							break;
						}
					}
					if (shipBase != null) {
						this.ship = new Ship(shipBase, quality);
					}
					break;
				}
			}
		}

		if (this.ship != null) {
			this.ui.setShipName(this.ship.getName());
			this.ui.setShipCrewCapacity(this.ship.getCrewCapacity());
			this.ui.setShipCargoCapacity(this.ship.getHoldCapacity());
			this.ui.setShipMaxHull(this.ship.getMaxHull());
			this.ui.setShipWeight(this.ship.getWeight());
			displayedQualities.add(this.ship.getTag());
		}
		
		// Process Companions to display assignments.
		List<QualityItem> companions = this.saveFileQualities.GetQualities(Companion.categoryName);
		if (companions != null) {
			for (QualityItem companion : companions) {
				this.processCompanion((QualityItem) companion);
			}
		}

		// Process Goods to display assignments.
		List<QualityItem> goods = this.saveFileQualities.GetQualities(Good.categoryName);
		this.cargoQuantity = 0;
		if (goods != null) {
			for (QualityItem good: goods) {
				this.processGood((Good) good);
			}
		}

		// Handle other objects in the save list.
		Map<Long, Map<String, Object>> saveQualityList = new HashMap<Long, Map<String, Object>>();

		// Construct a map of save-file qualities.
		for (Object qualityObject : qualities) {
			if (qualityObject instanceof Map<?, ?>) {
				Map<String, Object> quality = (Map<String, Object>) qualityObject;
				Object idObject = quality.get("AssociatedQualityId");
				if (idObject != null && idObject instanceof Long) {
					Long id = (Long) idObject;
					
					Object equippedPossesionObject = quality.get("EquippedPossession");
					if (equippedPossesionObject == null && !displayedQualities.contains(id)) {
						this.addOtherQualityToMap(id, quality, saveQualityList);
					}
				}
			}
		}

		// Now, process all categories and the known items in said categories.
		
		for (String category : QualityItem.getKnownCategories()) {
			// Need the items in this category sorted by Name.
			for (QualityItem item : QualityItem.getItemsInCategory(category)) {
				Map<String, Object> saveQuality = saveQualityList.get(item.getTag());
				if (saveQuality != null) {
					this.addQualityToUI(item, saveQuality);
					if (item.isCargo() && item.getSlot() == null) {
						this.cargoQuantity += JSONUtil.getLong(saveQuality, "Level");
					}
				} else {
					this.addQualityToUI(item);
				}
			}
		}

		this.cargoQuantity += JSONUtil.getLong(this.fuelQuality, "Level") + JSONUtil.getLong(this.suppliesQuality, "Level");
		this.ui.usedCapacity.setText(Long.toString(this.cargoQuantity));
	}

	/**
	 * Process a Companion quality. If the Companion is assigned to a Slot
	 * known on the UI, set the appropriate label.
	 * @param comp Companion.
	 */
	private void processCompanion(QualityItem comp)
	{
		if (comp.getSlot() != null) {
			switch (comp.getSlot()) {
			case "Cook":
				this.ui.cookLabel.setText(comp.getName());
				break;
			
			case "First Officer":
				this.ui.foLabel.setText(comp.getName());
				break;
			
			case "Chief Engineer":
				this.ui.engineerLabel.setText(comp.getName());
				break;
				
			case "Gunnery Officer":
				this.ui.gunneryLabel.setText(comp.getName());
				break;
				
			case "Mascot":
				this.ui.mascotLabel.setText(comp.getName());
				break;
				
			case "Surgeon":
				this.ui.surgeonLabel.setText(comp.getName());
				break;
			}				
		}
	}

	/**
	 * Process a Good. At the moment, only accumulates quantities of non-assigned Goods.
	 * In future, may display the Good on the UI.
	 * @param good
	 */
	private void processGood(Good good)
	{
		if (good.getSlot() != null && !good.getSlot().isEmpty())
		{
			switch(good.getSlot())
			{
				// Here, we could display this information someplace on the UI.
			}
		}
	}

	/**
	 * Load the configuration; this read the Quality definitions.
	 */
	@SuppressWarnings("unchecked")
	private void loadConfiguration()
	{
		// Static methods in the various QualityItem class don't run unless the class is created.
		Good.isQualityAGood(null);
		Ship.isQualityAShip(null);
		Companion.isQualityACompanion(null);
		
		String directory = this.getSunlessSeaDirectory("entities");
		if (directory != null) {
			File qualities = new File(directory + "/qualities.json");
			if (qualities.exists() && qualities.isFile() && qualities.canRead()) {
				try (InputStream input = new FileInputStream(qualities)) {
					Object list = jsonLib.deserialize(input, Object.class);
					if (list instanceof ArrayList<?>)
					{
						this.configurationQualities = new QualitiesList((ArrayList<Object>) list, true);
					}
				} catch (FileNotFoundException e) {
					this.ui.displayErrorDialog("A problem was encountered loading 'qualities.json': not found", "Error");
				} catch (IOException e) {
					this.ui.displayErrorDialog("A problem was encountered loading 'qualities.json': I/O error", "Error");
				}
			}
		}
		
		// Now make sure that the UI has all the category tabs.
		for (String category : QualityItem.getKnownCategories()) {
			this.ui.addPanel(category);
		}
		
		this.ui.addOtherPanel();
	}


	/**
	 * Add an other quality to the list. Added if known, or if showUnknown is true.
	 * @param id Quality ID.
	 * @param quality Quality definition.
	 * @param list Output list. Key is name or id.
	 */
	private void addOtherQualityToMap(long id, Map<String, Object> quality, Map<Long, Map<String, Object>> list)
	{
		Object value = JSONUtil.getKeyValue(quality, "Level");
		if (value != null) {
			QualityItem knownItem = QualityItem.getKnownItem((int) id);
			if (knownItem != null || this.showUnknown) {
				list.put(new Long(id), quality);
			}
		}
	}

	/**
	 * Add a Quality to the UI with a save-file value.
	 * @param item QualityItem; the AssociatedQualityId.
	 * @param saveQuality The Quality from the save file. This should contain a Level value.
	 */
	private void addQualityToUI(QualityItem item, Map<String, Object> saveQuality)
	{
		// Verify saveQuality. We already know that the saveQuality has an appropriate AssociatedQualityId.
		Object valueObject = JSONUtil.getKeyValue(saveQuality, "Level");
		if (valueObject != null && valueObject instanceof Long) {
			QualityItemUI field;
			field = this.ui.addToQualityPanel(item, saveQuality);
			this.qualitiesFieldsMap.put(saveQuality, field);
			field.addEventHandler(this);
		}
	}

	/**
	 * Add a Quality to the UI which has no associated save-file quality.
	 * @param item QualityItem; the AssociatedQualityId.
	 */
	private void addQualityToUI(QualityItem item)
	{
		// Verify saveQuality. We already know that the saveQuality has an appropriate AssociatedQualityId.
		QualityItemUI itemUI = this.ui.addToQualityPanel(item);
		itemUI.addEventHandler(this);
	}

	/**
	 * Get a specific quality object from the list.
	 * @param qualities Qualities list.
	 * @param itemKey Key of the quality.
	 * @return THe quality; or null if not found.
	 */
	private Map<String, Object> getQuality(ArrayList<Object> qualities, int itemKey)
	{
		Map<String, Object> result = null;
		for (Object qualityObject : qualities) {
			if (qualityObject instanceof Map<?, ?>) {
				@SuppressWarnings("unchecked")
				Map<String, Object> quality = (Map<String, Object>) qualityObject;
				Object id = quality.get("AssociatedQualityId");
				if (id != null && id instanceof Long && itemKey == (Long) id) {
					result = quality;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Set a UI field from a quality value, and return the associated Quality item.
	 * @param field UI field.
	 * @param qualities Qualities list.
	 * @param item Inventory item description.
	 * @return Quality item; null if not found.
	 */
	private Map<String, Object> setUIFromQuality(QualityItemUI field, ArrayList<Object> qualities, QualityItem item)
	{
		Map<String, Object> qualityItem = this.getQuality(qualities, (int) item.getTag());
		if (qualityItem != null) {
			this.setUIFromKey(field, qualityItem, "Level");
			field.addEventHandler(this);
		}

		return qualityItem;
	}

	/**
	 * Set a UI label from a key path in the JSON save.
	 * @param label Label to set.
	 * @param json JSON save.
	 * @param keys List of keys.
	 */
	private void setUIFromKey(QualityItemUI label, Map<String, Object> json, String...keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null) {
			label.setSaveItem(json);
		}
	}

	/**
	 * Set a UI label from a key path in the JSON save.
	 * @param label Label to set.
	 * @param json JSON save.
	 * @param keys List of keys.
	 */
	private void setUIFromKey(JLabel label, Map<String, Object> json, String...keys)
	{
		Object value = JSONUtil.getKeyValue(json, keys);
		if (value != null) {
			label.setText(value.toString());
		}
	}
	
	/**
	 * Update a quality from a UI field.
	 * @param quality Quality field.
	 * @param field UI field.
	 */
	private void setQualityFromUI(Map<String, Object> quality, QualityItemUI field)
	{
		if (quality != null) {
			try {
				Integer value = Integer.parseInt(field.getValue());
				quality.put("Level", value);
			} catch (NumberFormatException e) {
				// Not correct integer value input.
			}
		}
	}
	
	/**
	 * Get a directory in the Sunless Sea tree.
	 * @param subdirectory Subdirectory of interest 
	 * @return Directory; null if not known.
	 */
	private String getSunlessSeaDirectory(String subdirectory)
	{
		String appDataDirectory
		String osName = System.getProperty("os.name");

		if (osName.contains("OS X")) {
				appDataDirectory = (System.getProperty("user.home") + "/Library/Application Support/unity.Failbetter Games.Sunless Sea/");
			} else if (osName.contains("Windows")) {
				appDataDirectory = (System.getenv("APPDATA") + "/../LocalLow/Failbetter Games/Sunless Sea/");
			} else if (osName.contains("Linux")) {
				appDataDirectory = (System.getProperty("user.home") + "/.config/unity3d/Failbetter Games/Sunless Sea/");
			} else {
				appDataDirectory = null;
				System.out.println ("Not running on a supported operating system.");
			}
		}

		if (appDataDirectory != null) {
			return appDataDirectory + subdirectory;
		} else {
			return null;
		}
	}

	@Override
	public void onValueChanged(QualityItemUI ui, QualityItem item, Map<String, Object> saveItem)
	{
		if (item.isCargo()) {
			long oldValue = JSONUtil.getLong(saveItem, "Level");
			long newValue = Long.parseLong(ui.getValue());
			SSSaveEditor.this.cargoQuantity += newValue - oldValue;
			// Save item may be null for pre-defined Quality input fields (on the display panels).
			if (saveItem != null) {
				saveItem.put("Level",  new Long(newValue));
			}
			SSSaveEditor.this.ui.usedCapacity.setText(Long.toString(SSSaveEditor.this.cargoQuantity));
		}		
	}

	@Override
	public void onRequestAdd(QualityItemUI ui, QualityItem item)
	{
		Map<String, Object> newQuality = item.getTemplateObject();
		this.saveFileQualities.addQuality(newQuality);
		this.qualitiesFieldsMap.put(newQuality, ui);
		ui.mutate(newQuality);
	}

	@Override
	public void onRequestDelete(QualityItemUI ui, QualityItem item, Map<String, Object> saveItem)
	{
		this.saveFileQualities.removeQuality(saveItem);
		this.qualitiesFieldsMap.remove(saveItem);
		ui.mutate();
	}
}

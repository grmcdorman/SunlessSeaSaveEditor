package org.gmc.ssseditor.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import org.gmc.ssseditor.qualities.Companion;
import org.gmc.ssseditor.qualities.Good;
import org.gmc.ssseditor.qualities.ItemTags;
import org.gmc.ssseditor.qualities.QualityItem;

/**
 * This class contains only the UI for the save editor.
 * @author grant
 *
 */
public class SSSaveEditorUI {

	/**
	 * Needed for messages.
	 */
	public JFrame frame;
	static public NumberFormatter formatter;
	private TitledBorder titleBorder;
	private TitledBorder shipBorder;
	public JMenuItem saveMenuItem;
	public JMenuItem saveAsMenuItem;
	public JLabel currentPortLabel;
	public JLabel inGameDateLabel;
	public JLabel playerLabel;
	public JLabel mascotLabel;
	public JLabel surgeonLabel;
	public JLabel gunneryLabel;
	public JLabel cookLabel;
	public JLabel engineerLabel;
	public JLabel foLabel;
	public JLabel usedCapacity;

	public QualityItemUI shipCrewCapacityField;
	public QualityItemUI shipWeightField;
	public QualityItemUI shipMaxHullField;
	public QualityItemUI shipCargoCapacityField;
	public QualityItemUI echosField;
	public QualityItemUI fuelField;
	public QualityItemUI suppliesField;
	public QualityItemUI terrorField;
	public QualityItemUI hullField;
	public QualityItemUI mirrorsField;
	public QualityItemUI ironField;
	public QualityItemUI pagesField;
	public QualityItemUI heartsField;
	public QualityItemUI veilsField;
	public QualityItemUI crewField;
	
	private JTabbedPane tabs;
	private QualityUIPanel goodsPanel;
	private QualityUIPanel companionsPanel;
	private QualityUIPanel otherItemsPanel;
	private Map<String, QualityUIPanel> categoryPanels = new HashMap<String, QualityUIPanel>();

	private List<ISaveEditorEvents> eventHandlers = new LinkedList<ISaveEditorEvents>();

	public SSSaveEditorUI() {
		super();
	}

	public interface ISaveEditorEvents {
		public void onFileOpen();
		public void onFileSave();
		public void onFileSaveAs();
	};


	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize()
	{
		NumberFormat format = NumberFormat.getInstance();
	    SSSaveEditorUI.formatter = new NumberFormatter(format);
	    SSSaveEditorUI.formatter.setValueClass(Integer.class);
	    SSSaveEditorUI.formatter.setMinimum(0);
	    SSSaveEditorUI.formatter.setMaximum(Integer.MAX_VALUE);
	    SSSaveEditorUI.formatter.setAllowsInvalid(false);
	    
	    this.frame = new JFrame();

	    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.frame.setResizable(true);

		this.createMenuBar();
		
		JPanel dataPanel = new JPanel();
		this.frame.setContentPane(dataPanel);
		dataPanel.setLayout(new GridBagLayout());

		int row = 0;

		// * Summary.
		this.titleBorder = BorderFactory.createTitledBorder("Open a save file");
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new GridLayout());
		titlePanel.setBorder(this.titleBorder);
		this.playerLabel = this.addLabeledDisplay(titlePanel, "Zee Captain", row, 0);
		this.currentPortLabel = this.addLabeledDisplay(titlePanel, "Current Port", row, 2);
		this.inGameDateLabel = this.addLabeledDisplay(titlePanel, "In-Game Date", row, 4);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		dataPanel.add(titlePanel, constraints);
		
		++row;
		this.createShipPanel(dataPanel, row, 0);
		this.createAssignedOfficerPanel(dataPanel, row, 1);

		++row;
		this.createAttributesPanel(dataPanel, row, 0);
		this.createBasicInventoryPanel(dataPanel, row, 1);

		++row;
		tabs = new JTabbedPane();
		this.goodsPanel = new QualityUIPanel(tabs, "Goods");
		this.companionsPanel = new QualityUIPanel(tabs, "Companions");

		this.categoryPanels.put(Good.categoryName, goodsPanel);
		this.categoryPanels.put(Companion.categoryName, companionsPanel);

		constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = row;
		dataPanel.add(tabs, constraints);
		this.frame.pack();
	}

	public void addEventHandler(ISaveEditorEvents handler)
	{
		this.eventHandlers.add(handler);
	}
	
	public void removeEventHandler(ISaveEditorEvents handler)
	{
		this.eventHandlers.remove(handler);
	}

	public void setVisible()
	{
		this.frame.setVisible(true);
	}

	/**
	 * Set the title text.
	 * @param text New title text.
	 */
	public void setTitle(String text)
	{
		this.titleBorder.setTitle(text);
	}

	/**
	 * Set the ship name on the Ship panel.
	 * @param text New Ship name.
	 */
	public void setShipName(String text)
	{
		this.shipBorder.setTitle(text);
	}

	/**
	 * Add a category panel, if it does not already exist.
	 * @param category Category panel name.
	 */
	public void addPanel(String category)
	{
		if (!this.categoryPanels.containsKey(category))
		{
			this.categoryPanels.put(category, new QualityUIPanel(tabs, category));
		}
	}

	/**
	 * Add the 'other items' panel for uncategorized items.
	 */
	public void addOtherPanel()
	{
		this.otherItemsPanel = new QualityUIPanel(tabs, "Other");
	}

	/**
	 * Clear the content of panels.
	 */
	public void clearPanels()
	{
		for (QualityUIPanel panel : this.categoryPanels.values())
		{
			panel.reset();
		}
		this.otherItemsPanel.reset();
	}

	/**
	 * Add a Quality display to a quality panel, in the input state.
	 * @param item Quality item.
	 * @param saveItem Associated save item.
	 * @return New QualityItemUI.
	 */
	public QualityItemUI addToQualityPanel(QualityItem item, Map<String, Object> saveItem)
	{
		QualityUIPanel panel = this.categoryPanels.get(item.getCategory());
		return panel != null ? panel.add(item, saveItem) : null;
	}

	/**
	 * Add a Quality display to a quality panel, with a button for adding it to the save.
	 * @param item Quality item.
	 * @return New QualityItemUI.
	 */
	public QualityItemUI addToQualityPanel(QualityItem item)
	{
		QualityUIPanel panel = this.categoryPanels.get(item.getCategory());
		return panel != null ? panel.add(item) : null;
	}

	/**
	 * Add a Quality display to the 'uncategorized items' panel.
	 * @param labelText Label to display.
	 * @param value Initial value for the display.
	 * @return Text input for this item.
	 */
	protected QualityItemUI addOtherQuality(QualityItem item, Map<String, Object> saveItem)
	{
		return this.otherItemsPanel.add(item, saveItem);
	}

	/**
	 * Create the menu bar.
	 */
	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		JMenuItem openMenuItem = new JMenuItem("Open...");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SSSaveEditorUI.this.onFileOpen();
			}
		});
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		fileMenu.add(openMenuItem);
		
		JSeparator separator_2 = new JSeparator();
		fileMenu.add(separator_2);
		
		this.saveMenuItem = new JMenuItem("Save");
		this.saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SSSaveEditorUI.this.onFileSave();
			}
		});
		this.saveMenuItem.setEnabled(false);
		this.saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		fileMenu.add(this.saveMenuItem);
		
		this.saveAsMenuItem = new JMenuItem("Save As....");
		this.saveAsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SSSaveEditorUI.this.onFileSaveAs();
			}
		});
		this.saveAsMenuItem.setEnabled(false);
		fileMenu.add(this.saveAsMenuItem);
		
		JSeparator separator_1 = new JSeparator();
		fileMenu.add(separator_1);
		
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);

		// TODO: This should contain an About item and should be right-flush.
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
	}

	public int displayWarningDialog(String message, String title)
	{
		return JOptionPane.showConfirmDialog(this.frame, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
	}
	
	public void displayMessageDialog(String message, String title)
	{
		JOptionPane.showMessageDialog(this.frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void displayErrorDialog(String message, String title)
	{
		
	}

	private void onFileOpen()
	{
		for (ISaveEditorEvents handler : this.eventHandlers)
		{
			handler.onFileOpen();
		}
	}

	private void onFileSave()
	{
		for (ISaveEditorEvents handler : this.eventHandlers)
		{
			handler.onFileSave();
		}
	}


	private void onFileSaveAs()
	{
		for (ISaveEditorEvents handler : this.eventHandlers)
		{
			handler.onFileSaveAs();
		}
	}
	/**
	 * Create the basic inventory panel, containing echos, fuel, and supplies.
	 * @param dataPanel Parent panel.
	 * @param row Row in parent panel.
	 * @param col Column in parent panel.
	 * @return Inventory panel.
	 */
	private int createBasicInventoryPanel(JPanel dataPanel, int row, int col)
	{
		JPanel basicInventoryPanel = this.createDisplayPanel("Basic Inventory");
		int inventoryRow = 0;
		this.echosField = new QualityItemUI(basicInventoryPanel, ItemTags.echos.getName(), inventoryRow, 0);
		this.fuelField = new QualityItemUI(basicInventoryPanel, ItemTags.fuel.getName(), inventoryRow, 2);
		
		++inventoryRow;
		this.suppliesField = new QualityItemUI(basicInventoryPanel, ItemTags.supplies.getName(), inventoryRow, 0);

		++inventoryRow;

		this.addDisplayPanel(dataPanel, row, col, basicInventoryPanel, inventoryRow);

		return inventoryRow;
	}

	/**
	 * Create the attributes panel.
	 * @param dataPanel Parent panel.
	 * @param row Row in parent panel.
	 * @param col Column in parent panel.
	 * @return Attributes panel.
	 */
	private int createAttributesPanel(JPanel dataPanel, int row, int col)
	{
		JPanel attributesPanel = this.createDisplayPanel("Attributes");
		int attributesRow = 0;
		this.terrorField = new QualityItemUI(attributesPanel, ItemTags.terror.getName(), attributesRow, 0);
		this.hullField = new QualityItemUI(attributesPanel, ItemTags.hull.getName(), attributesRow, 2);

		++attributesRow;
		this.mirrorsField = new QualityItemUI(attributesPanel, ItemTags.mirrors.getName(), attributesRow, 0);
		this.ironField = new QualityItemUI(attributesPanel, ItemTags.iron.getName(), attributesRow, 2);


		++attributesRow;
		this.pagesField = new QualityItemUI(attributesPanel, ItemTags.pages.getName(), attributesRow, 0);
		this.heartsField = new QualityItemUI(attributesPanel, ItemTags.hearts.getName(), attributesRow, 2);


		++attributesRow;
		this.veilsField = new QualityItemUI(attributesPanel, ItemTags.veils.getName(), attributesRow, 0);
		this.crewField = new QualityItemUI(attributesPanel, ItemTags.crew.getName(), attributesRow, 2);


		++attributesRow;

		this.addDisplayPanel(dataPanel, row, col, attributesPanel, attributesRow);
		return attributesRow;
	}

	/**
	 * Create the assigned officer panel.
	 * @param dataPanel Parent panel.
	 * @param row Row in parent panel.
	 * @param col Column in parent panel.
	 * @return Officer panel.
	 */
	private int createAssignedOfficerPanel(JPanel dataPanel, int row, int col)
	{
		JPanel officerPanel = this.createDisplayPanel("Assigned Officers");
		int officerRow = 0;
		this.mascotLabel = this.addLabeledDisplay(officerPanel, "Mascot", officerRow, 0);
		this.surgeonLabel = this.addLabeledDisplay(officerPanel, "Surgeon", officerRow, 2);
		
		++officerRow;
		
		this.gunneryLabel = this.addLabeledDisplay(officerPanel, "Gunner", officerRow, 0);
		this.cookLabel = this.addLabeledDisplay(officerPanel, "Cook", officerRow, 2);
		
		++officerRow;
		
		this.engineerLabel = this.addLabeledDisplay(officerPanel, "Engineer", officerRow, 0);
		this.foLabel = this.addLabeledDisplay(officerPanel, "First Officer", officerRow, 2);

		++officerRow;

		this.addDisplayPanel(dataPanel, row, col, officerPanel, officerRow);
		return officerRow;
	}

	/**
	 * Create the ship panel, containing basic ship details.
	 * @param dataPanel Parent panel.
	 * @param row Row in parent panel.
	 * @param col Column in parent panel.
	 * @return Ship panel.
	 */
	private int createShipPanel(JPanel dataPanel, int row, int col)
	{
		JPanel shipPanel = createDisplayPanel("Ship");
		this.shipBorder = (TitledBorder) shipPanel.getBorder();

		// *** Ship.
		int shipRow = 0;
		this.shipCrewCapacityField = new QualityItemUI(shipPanel, "Crew Capacity", shipRow, 0);
		this.shipMaxHullField = new QualityItemUI(shipPanel, "Maximum Hull", shipRow, 2);
		
		++shipRow;
		this.shipCargoCapacityField = new QualityItemUI(shipPanel, "Cargo Capacity", shipRow, 0);
		this.shipWeightField = new QualityItemUI(shipPanel, "Weight", shipRow, 2);

		++shipRow;

		this.usedCapacity = this.addLabeledDisplay(shipPanel, "Cargo", shipRow, 0);
		
		++shipRow;
		this.addDisplayPanel(dataPanel, row, col, shipPanel, shipRow);
		return shipRow;
	}

	/**
	 * Create a generic display panel.
	 * @param title Title for the panel.
	 * @return New panel.
	 */
	private JPanel createDisplayPanel(String title)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(title));
		return panel;
	}

	/**
	 * Add a display panel to a parent panel.
	 * @param dataPanel Parent panel
	 * @param row Row in parent panel.
	 * @param col Column in parent panel.
	 * @param displayPanel Panel to add.
	 * @param rows Row size of panel.
	 */
	private void addDisplayPanel(JPanel dataPanel, int row, int col, JPanel displayPanel, int rows)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = col;
		constraints.gridy = row;
		dataPanel.setPreferredSize(new Dimension(1200, 300*rows));
		dataPanel.add(displayPanel, constraints);
	}

	/**
	 * Add a labeled display to a data panel.
	 * @param dataPanel Data panel.
	 * @param labelText Text for the label.
	 * @param row Row for the display.
	 * @param col Column for the display.
	 * @return Value label.
	 */
	private JLabel addLabeledDisplay(JPanel dataPanel, String labelText, int row, int col)
	{
		JLabel titleLabel = SSSaveEditorUI.addLabel(dataPanel, labelText, row, col);
		JLabel valueLabel = SSSaveEditorUI.addLabel(dataPanel, "", row, col + 1, 0, GridBagConstraints.WEST);
		valueLabel.setBorder(BorderFactory.createLoweredBevelBorder());
		titleLabel.setLabelFor(valueLabel);

		return valueLabel;
	}

	/**
	 * Add a labeled numeric input to a data panel.
	 * @param dataPanel Data panel.
	 * @param labelText Text for the label.
	 * @param row Row for the display.
	 * @param col Column for the display.
	 * @return Numeric input field.
	 */
	public static JFormattedTextField addLabeledNumericInput(JPanel dataPanel, String labelText, int row, int col)
	{
		JLabel titleLabel = SSSaveEditorUI.addLabel(dataPanel, labelText, row, col);
		JFormattedTextField field = new JFormattedTextField(SSSaveEditorUI.formatter);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 5, 0, 0);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = col + 1;
		constraints.gridy = row;
		dataPanel.add(field, constraints);
		field.setColumns(10);

		titleLabel.setLabelFor(field);

		return field;
	}

	/**
	 * Add a label to a panel.
	 * @param dataPanel Parent panel.
	 * @param text Label text.
	 * @param row Row for label.
	 * @param col Column for label.
	 * @return Label.
	 */
	public static JLabel addLabel(JPanel dataPanel, String text, int row, int col)
	{
		return SSSaveEditorUI.addLabel(dataPanel, text, row, col, 0, GridBagConstraints.EAST);
	}

	/**
	 * Add a label to a panel.
	 * @param dataPanel Parent panel.
	 * @param text Label text.
	 * @param row Row for label.
	 * @param col Column for label.
	 * @param gridWidth Grid width of label.
	 * @param anchor Anchor of label.
	 * @return Label.
	 */
	private static JLabel addLabel(JPanel dataPanel, String text, int row, int col, int gridWidth, int anchor)
	{
		JLabel label = new JLabel(text);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = anchor;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.insets = new Insets(0, 10, 0, 5);
		if (gridWidth > 0) {
			constraints.gridwidth = gridWidth;
		}
		constraints.gridx = col;
		constraints.gridy = row;
		dataPanel.add(label, constraints);
		
		return label;
	}
}
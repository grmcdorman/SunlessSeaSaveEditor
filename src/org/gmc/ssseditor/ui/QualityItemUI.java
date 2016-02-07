package org.gmc.ssseditor.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.gmc.ssseditor.JSONUtil;
import org.gmc.ssseditor.qualities.QualityItem;

/**
 * This class encapsulates a QualityItem on the UI. It can be in one of two states:
 * <ul>
 * <li>Not added: the label and an 'add' button are shown</li>
 * <li>Added: the label, a text field with an integer value, and a 'delete' button are shown</li>
 * </ul>
 * @author grant
 *
 */
public class QualityItemUI {
	public static interface IQualityItemUIEvents
	{
		public void onValueChanged(QualityItemUI ui, QualityItem item, Map<String, Object> saveItem);
		public void onRequestAdd(QualityItemUI ui, QualityItem item);
		public void onRequestDelete(QualityItemUI ui, QualityItem item, Map<String, Object> saveItem);
	}
	
	private QualityItem item;
	private Map<String, Object> saveItem;
	private JButton addButton;
	private JButton deleteButton;
	private JFormattedTextField valueField;
	private List<IQualityItemUIEvents> eventHandlers = new LinkedList<IQualityItemUIEvents>();

	/**
	 * Construct a new QualityItemUI instance with a save file Quality.
	 * @param panel Parent panel for UI components.
	 * @param item QualityItem.
	 * @param saveItem Save file Quality.
	 * @param row Row for UI in parent panel.
	 */
	public QualityItemUI(JPanel panel, QualityItem item, Map<String, Object> saveItem, int row)
	{
		this.item = item;

		this.constructUI(panel, row, 0, item.getName());
		this.mutate(saveItem);
	}
	
	/**
	 * Construct a new QualityItemUI instance with no save file Quality.
	 * @param panel Parent panel 
	 * @param panel Parent panel for UI components.
	 * @param item QualityItem.
	 * @param row Row for UI in parent panel.
	 */
	public QualityItemUI(JPanel panel, QualityItem item, int row)
	{
		this.item = item;
		this.saveItem = null;

		this.constructUI(panel, row, 0, item.getName());
		this.mutate();
	}

	/**
	 * Package constructor for information panels outside of the Quality lists.
	 * @param panel Parent panel for UI components.
	 * @param item QualityItem.
	 * @param saveItem Save file Quality.
	 * @param row Row for UI in parent panel.
	 * @param col Column for UI in parent panel.
	 */
	QualityItemUI(JPanel panel, QualityItem item, int row, int col)
	{
		this.item = item;
		this.saveItem = null;
		this.constructUI(panel, row, col, item.getName());
		this.hideButtons();
	}

	/**
	 * Package constructor for information panels outside of the Quality lists.
	 * @param panel Parent panel for UI components.
	 * @param item QualityItem.
	 * @param saveItem Save file Quality.
	 * @param row Row for UI in parent panel.
	 * @param col Column for UI in parent panel.
	 */
	QualityItemUI(JPanel panel, String name, int row, int col)
	{
		this.item = null;
		this.saveItem = null;
		this.constructUI(panel, row, col, name);
		this.hideButtons();
	}

	/**
	 * Add an event handler for QualityItemUI events.
	 * @param eventHandler Event handler.
	 */
	public void addEventHandler(IQualityItemUIEvents eventHandler)
	{
		this.eventHandlers.add(eventHandler);
	}
	
	/**
	 * Remove an event handler.
	 * @param eventHandler Event handler.
	 */
	public void removeEventHandler(IQualityItemUIEvents eventHandler)
	{
		this.eventHandlers.remove(eventHandler);
	}

	/**
	 * Mutate the UI to the save-file instance state.
	 * @param saveItem Associated save item.
	 */
	public void mutate(Map<String, Object> saveItem)
	{
		this.saveItem = saveItem;
		
		this.valueField.setText(Long.toString(JSONUtil.getLong(saveItem, "Level")));
		this.addButton.setVisible(false);
		this.valueField.setVisible(true);
		this.deleteButton.setVisible(true);
	}

	/**
	 * Mutate the UI to the no-save-file instance state.
	 */
	public void mutate()
	{
		this.saveItem = null;

		this.valueField.setVisible(false);
		this.deleteButton.setVisible(false);
		this.addButton.setVisible(true);
	}

	/**
	 * Get the value of the input field.
	 * @return Value.
	 */
	public String getValue()
	{
		return this.valueField.getText();
	}

	/**
	 * Set the value of the text field from a string.
	 * @param newValue New value.
	 */
	public void setValue(String newValue)
	{
		this.valueField.setText(newValue);
	}

	/**
	 * Set the value of the text field from a Quality.
	 * @param quality Source quality.
	 */
	public void setValue(Map<String, Object> quality)
	{
		this.setValue(Long.toString(JSONUtil.getLong(quality, "Level")));
	}
	
	/**
	 * Set the value of the text field from the associated quality.
	 */
	public void update()
	{
		this.setValue(this.saveItem);
	}

	public void setSaveItem(Map<String, Object> newItem)
	{
		this.saveItem = newItem;
		this.update();
	}

	/**
	 * Hide all buttons. Used when the UI is in a static display panel, such as the Ship panel.
	 */
	void hideButtons()
	{
		this.addButton.setVisible(false);
		this.deleteButton.setVisible(false);
	}

	/**
	 * Handle the push of the Add button.
	 */
	private void onAdd()
	{
		for (IQualityItemUIEvents handler : this.eventHandlers)
		{
			handler.onRequestAdd(this, this.item);
		}
	}
	
	/**
	 * Handle changes to the value field.
	 */
	private void onChange()
	{
		if (!this.valueField.getText().isEmpty())
		{
			try
			{
				Integer.parseInt(this.valueField.getText());
				for (IQualityItemUIEvents handler : this.eventHandlers)
				{
					handler.onValueChanged(this, this.item, this.saveItem);
				}
			} catch (NumberFormatException e) {
				// Ignore it. The formatter should fix it on focus-out or similar.
			}
		}
	}
	
	/**
	 * Handle the push of the Delete button.
	 */
	private void onDelete()
	{
		for (IQualityItemUIEvents handler : this.eventHandlers)
		{
			handler.onRequestDelete(this, this.item, this.saveItem);
		}
	}
	
	/**
	 * Construct the full UI. The caller must hide the appropriate portions of the UI.
	 * @param panel Parent panel.
	 * @param row Row in parent panel.
	 * @param baseColumn Base column for elements.
	 */
	private void constructUI(JPanel panel, int row, int baseColumn, String name)
	{
		this.valueField = SSSaveEditorUI.addLabeledNumericInput(panel, name, row, baseColumn);
		this.valueField.setText("-unset-");

		// Add a 'delete' button.
		this.deleteButton = new JButton("-");
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 5, 0, 0);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = baseColumn + 2;
		constraints.gridy = row;
		panel.add(this.deleteButton, constraints);

		// Add an 'add' button.
		this.addButton = new JButton("+");
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 5, 0, 0);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = baseColumn + 1;
		constraints.gridy = row;
		panel.add(this.addButton, constraints);

		this.valueField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				QualityItemUI.this.onChange();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				QualityItemUI.this.onChange();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				QualityItemUI.this.onChange();
			}
		});

		this.deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				QualityItemUI.this.onDelete();
			}
		});

		this.addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				QualityItemUI.this.onAdd();
			}
		});
	}

	public int getHeight()
	{
		return this.addButton.isShowing() ? this.addButton.getHeight() : this.deleteButton.getHeight();
	}
}

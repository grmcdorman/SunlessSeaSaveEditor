package org.gmc.ssseditor.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.gmc.ssseditor.qualities.QualityItem;

/**
 * This class contains a panel intended to hold data for one Quality category. It is shown as a scrolled window.
 * @author grant
 *
 */
public class QualityUIPanel implements ComponentListener
{
	private JPanel itemPanel;
	private JScrollBar verticalBar;
	private int addRow = 0;
	private QualityItemUI lastAddedField = null;

	public QualityUIPanel(JTabbedPane tabs, String labelString)
	{
		JPanel fillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		this.itemPanel = new JPanel();
		this.itemPanel.setLayout(new GridBagLayout());
		fillPanel.add(this.itemPanel);
		JScrollPane scrollPane = new JScrollPane(fillPanel);

		scrollPane.setPreferredSize(new Dimension(200, 200));
		tabs.addTab(labelString, scrollPane);
		
		this.verticalBar = scrollPane.getVerticalScrollBar();
		this.itemPanel.addComponentListener(this);
	}
	
	public JPanel getPanel()
	{
		return this.itemPanel;
	}
	
	/**
	 * Add an input field for a quantity item.
	 * @param item QualityItem.
	 * @param saveItem Save-file Quality.
	 * @return New QualityItemUI.
	 */
	public QualityItemUI add(QualityItem item, Map<String, Object> saveItem)
	{
		QualityItemUI ui = new QualityItemUI(this.itemPanel, item, saveItem, this.addRow);
		
		++this.addRow;

		this.lastAddedField = ui;

		return ui;
	}

	/**
	 * Add a QualityItemUI in the non-save-file state.
	 * @param item QualityItem.
	 * @return New QualityItemUI.
	 */
	public QualityItemUI add(QualityItem item)
	{
		QualityItemUI ui = new QualityItemUI(this.itemPanel, item, this.addRow);
		
		++this.addRow;

		this.lastAddedField = ui;

		return ui;
	}

	/**
	 * Reset the panel by clearing all items and preparing to add new items.
	 */
	public void reset()
	{
		this.itemPanel.removeAll();
		this.addRow = 0;
	}


	@Override
	public void componentShown(ComponentEvent e)
	{
		// Not used, required by ComponentListener interface.
	}
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		if (this.lastAddedField != null && this.lastAddedField != null && this.lastAddedField.getHeight() != 0) {
			this.verticalBar.setUnitIncrement(lastAddedField.getHeight());
			this.itemPanel.removeComponentListener(this);
		}
	}
	
	@Override
	public void componentMoved(ComponentEvent e)
	{
		// Not used, required by ComponentListener interface.
	}
	
	@Override
	public void componentHidden(ComponentEvent e)
	{
		// Not used, required by ComponentListener interface.
	}
}

package dk.itu.big_red.wizards;

/**
 * Wizards which require the user to choose a port should implement
 * IPortSelector and should incorporate a PortSelectionPage.
 * @author alec
 *
 */
public interface IPortSelector {
	public void setSelectedPort(String port);
	public String getSelectedPort();
}

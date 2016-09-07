package com.esgi.vMail.control;

import com.esgi.vMail.control.event.EventOnConnectionListChange;
import com.esgi.vMail.control.event.ListenOnRosterChange;
import com.esgi.vMail.model.*;
import com.esgi.vMail.model.DAO.DAO_Connection_XML;
import com.esgi.vMail.view.option_controler.OptionConnectionListManager.ServerLine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jivesoftware.smack.AbstractConnectionClosedListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.stream.Collectors;

public class ConnectionManager {
	private static ObservableMap<Jid, AccountManager> ownerList = FXCollections.observableHashMap();
	private static ObservableList<Connection> connectionList;
	private static ObservableList<ServerLine> displayedConnectionList;
	private static ObservableList<Group> groupList = FXCollections.observableArrayList();
	private static ObservableMap<Contact, Chat> contactMap = FXCollections.observableHashMap();

	static {
		ConnectionManager.connectionList = ConnectionManager.importConnectionListFromXML();
		ConnectionManager.initRostersAndChat();
		ConnectionManager.loginEnabledConnection();
		ConnectionManager.connectionList.addListener(new EventOnConnectionListChange());
	}

	public static void init() {

	}

	private static void initRostersAndChat() {
		for (Connection connection : connectionList) {
			connection.setRoster(Roster.getInstanceFor(connection));
			connection.setChatManager(ChatManager.getInstanceFor(connection));
			connection.getRoster().setRosterLoadedAtLogin(false);
			connection.getRoster().addRosterListener(new ListenOnRosterChange(connection));
			UserSearch userSearch = new UserSearch();
		}
	}

	public static ObservableList<Connection> importConnectionListFromXML() {
		ObservableList<Connection> connectionList = FXCollections.observableArrayList();
		connectionList.addAll(DAO_Connection_XML.getAll2ConnectionConf().stream().map(Connection::new).collect(Collectors.toList()));
		return connectionList;
	}

	/**
	 * @return the connectionList
	 */
	public static ObservableList<Connection> getConnectionList() {
		return connectionList;
	}

	public static ObservableList<Group> getGroupList() {
		return groupList;
	}

	public static ObservableMap<Contact, Chat> getContactMap() {
		return contactMap;
	}

	public static ObservableMap<Jid, AccountManager> getOwnerList() {
		return ownerList;
	}

	public static Contact getContactByJID(Jid JID) {
		Contact[] contacts = new Contact[ConnectionManager.getContactMap().keySet().size()];
		ConnectionManager.getContactMap().keySet().toArray(contacts);
		for (Contact contact : contacts) {
			if (contact.getEntry().getJid().equals(JID.asBareJid())) {
				return contact;
			}
		}
		return null;
	}

	/**
	 * @param displayedConnectionList the displayedConnectionList to set
	 */
	public static void setDisplayedConnectionList(ObservableList<ServerLine> displayedConnectionList) {
		ConnectionManager.displayedConnectionList = displayedConnectionList;
	}

	/**
	 * @return the displayedConnectionList
	 */
	public static ObservableList<ServerLine> getDisplayedConnectionList() {
		return displayedConnectionList;
	}

	public static boolean reconnect(Connection connection) {
		try {
			connection.connect();
			connection.login();
			Thread.sleep(1000);
			return connection.isAuthenticated();
		} catch (SmackException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isConnectionValid(Connection connection) {
		try {
			connection.connect();
			return connection.isConnected();
		} catch (SmackException | IOException | XMPPException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connection.disconnect();
		}
		return false;
	}

	private static synchronized void loginEnabledConnection() {
		for (Connection connection : connectionList.filtered((connectionT) -> connectionT.isEnabled())) {
			try {
				connection.connect();
				connection.login();
				connection.addConnectionListener(new AbstractConnectionClosedListener() {
					@Override
					public void connectionTerminated() {
						ConnectionManager.reconnect(connection);
					}
				});
				Thread.sleep(1000);
				ownerList.put(connection.getUser(), AccountManager.getInstance(connection));
				connection.getRoster().reload();
				Presence presence = new Presence(Presence.Type.subscribe);
				connection.sendStanza(presence);
				presence.setType(Type.available);
				connection.sendStanza(presence);
				connection.setPacketReplyTimeout(30000);
			} catch (SmackException | IOException | XMPPException | InterruptedException e) {
				// TODO Auto-generated catch block
				connection.getStatusMsg().set(e.getMessage());
			}
		}

	}

}

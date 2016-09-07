package com.esgi.vMail.view_controler;

import com.esgi.vMail.control.ConnectionManager;
import com.esgi.vMail.control.LangManager;
import com.esgi.vMail.model.Chat;
import com.esgi.vMail.model.Connection;
import com.esgi.vMail.model.Contact;
import com.esgi.vMail.model.Contact.ContactListView;
import com.esgi.vMail.view.OptionsWindow;
import com.esgi.vMail.view.StatusRound;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupBuilder;
import javafx.util.Pair;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.util.PacketUtil;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smackx.muc.packet.MUCInitialPresence;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Optional;

public class MainWindowManager extends ManagerBuilder {

	@FXML
	private ImageView imgAvatar;

	@FXML
	private TabPane tabConvList;

	@FXML
	private Label lblPseudo;

	@FXML
	private MenuItem menuIOption;

	@FXML
	private MenuItem menuIAddContact;


	@FXML
	private Accordion groupListView;

	@FXML
	private Menu menuStatus;

	@FXML
	private Circle menuRoundStatus;

	@FXML
	private MenuItem menuStatusOptionOnline;
	@FXML
	private MenuItem menuStatusOptionAway;
	@FXML
	private MenuItem menuStatusOptionBusy;
	@FXML
	private MenuItem menuStatusOptionOffline;
	@FXML
	private MenuItem menuStatusOptionCustom;


	@FXML
	public void callOptionPane() {
		OptionsWindow optionsWindow = new OptionsWindow();
		optionsWindow.getWindowStage().initModality(Modality.WINDOW_MODAL);
		optionsWindow.getWindowStage().initOwner(this.windowBuilder.getWindowStage());
		optionsWindow.getWindowStage().show();
	}

	@FXML
	public void addContact() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setContentText(LangManager.text("vMail.menu.contact.popup.jid"));
		dialog.setTitle(LangManager.text("vMail.menu.contact.popup.title"));
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(this.windowBuilder.getWindowStage());
		Optional<String> result = dialog.showAndWait();
		result. ifPresent(jidString -> ConnectionManager.getConnectionList().forEach(connection -> {
			try {
				BareJid jid = JidCreate.bareFrom(jidString);
				Presence response = new Presence(Presence.Type.subscribed);
				if (connection.getRoster().contains(jid)) {
					response.setTo(jid);
					connection.sendStanza(response);
				}
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (XmppStringprepException e) {
				addContact();
			}

		}));

	}

	private void sendPresence(Presence presence) {
		ConnectionManager.getConnectionList().filtered(connection -> connection.isAuthenticated()).forEach(connectionAuth -> {
			try {
				connectionAuth.sendStanza(presence);
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	private void modifyDisplayedPresence(Presence presence) {
		StatusRound.update(menuRoundStatus, presence);
		menuStatus.setText(presence.getStatus());
	}

	@FXML
	private void initialize() {
		menuStatusOptionOnline.setOnAction(event -> {
			Presence presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.available);
			presence.setStatus(LangManager.text("vMail.menu.status.online"));
			sendPresence(presence);
			modifyDisplayedPresence(presence);
		});

		menuStatusOptionAway.setOnAction(event -> {
			Presence presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.away);
			presence.setStatus(LangManager.text("vMail.menu.status.away"));
			sendPresence(presence);
			modifyDisplayedPresence(presence);
		});

		menuStatusOptionBusy.setOnAction(event -> {
			Presence presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.dnd);
			presence.setStatus(LangManager.text("vMail.menu.status.busy"));
			sendPresence(presence);
			modifyDisplayedPresence(presence);
		});

		menuStatusOptionOffline.setOnAction(event -> {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus(LangManager.text("vMail.menu.status.offline"));
			sendPresence(presence);
			modifyDisplayedPresence(presence);
		});

		menuStatusOptionCustom.setOnAction(event -> {
			Dialog<Pair<String, String>> custoPresence = new Dialog<>();
			custoPresence.setTitle("Custom Presence");
			custoPresence.setHeaderText("Choose a custom presence:");

			custoPresence.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));
			Label lblType = new Label("Type:");
			ChoiceBox<String> cbType = new ChoiceBox<>();
			cbType.setItems(FXCollections.observableArrayList(
					LangManager.text("vMail.menu.status.online"),
					LangManager.text("vMail.menu.status.away"),
					LangManager.text("vMail.menu.status.busy")
			));
			cbType.getSelectionModel().select(0);
			Label lblMessage = new Label("Message:");
			TextArea txtAMsg = new TextArea();
			grid.add(lblType, 0, 0);
			grid.add(cbType, 1, 0);
			grid.add(lblMessage, 0, 1);
			grid.add(txtAMsg, 1, 1);
			custoPresence.getDialogPane().setContent(grid);
			custoPresence.setResultConverter(dialogButton -> {
				if (dialogButton.equals(ButtonType.OK)) {
					return new Pair<>(cbType.getValue(), txtAMsg.getText());
				}
				return null;
			});
			custoPresence.showAndWait().ifPresent(resultPair -> {
				Presence presence = new Presence(Presence.Type.available);
				if (resultPair.getKey().equals(LangManager.text("vMail.menu.status.online"))) {
					presence.setMode(Presence.Mode.available);
				} else if (resultPair.getKey().equals(LangManager.text("vMail.menu.status.away"))) {
					presence.setMode(Presence.Mode.away);
				} else if (resultPair.getKey().equals(LangManager.text("vMail.menu.status.busy"))) {
					presence.setMode(Presence.Mode.dnd);
				}
				presence.setStatus(resultPair.getValue());
				sendPresence(presence);
				modifyDisplayedPresence(presence);
			});
		});

		ConnectionManager.getGroupList().forEach((group) -> this.groupListView.getPanes().add(group.asGroupTitledPane()));
		for (Chat chat : ConnectionManager.getContactMap().values()) {
			//tabConvList.getTabs().add(chat.asChatTab());
			chat.getChatXMPP().addMessageListener((chat1, message) -> {
                if (!ConnectionManager.getContactMap().get(ConnectionManager.getContactByJID(chat1.getParticipant())).hasAView()) {
                    tabConvList.getTabs().add(ConnectionManager.getContactMap().get(ConnectionManager.getContactByJID(chat1.getParticipant())).asChatTab());
                }
            });
		}
//		groupListView.getPanes().forEach((group) -> {
//			((ListView<Contact.ContactListView>) group.getContent()).getItems().forEach((contact) -> {
//				contact.setOnMouseClicked((event) -> {
//					if (event.getClickCount() == 2) {
//						if (!ConnectionManager.getContactMap().get(contact.getObjectModel()).hasAView()) {
//							Chat chat = ConnectionManager
//									.getContactMap()
//									.get(contact.getObjectModel());
//							ChatTab chatTab = chat.asChatTab();
//							ObservableList<Tab> tabs = tabConvList.getTabs();
//							tabs.add(chatTab);
//							tabConvList.getSelectionModel().select(chatTab);
//							event.consume();
//						}
//					}
//				});
//			});
//		});
		for (TitledPane group : groupListView.getPanes()) {
			ListView<Contact.ContactListView> listContacts = (ListView<Contact.ContactListView>) group.getContent();
			for (ContactListView contact : listContacts.getItems()) {
				contact.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        if (!ConnectionManager.getContactMap().get(contact.getObjectModel()).hasAView()) {
                            Chat chat = ConnectionManager
                                    .getContactMap()
                                    .get(contact.getObjectModel());
                            System.out.println(chat.getChatXMPP().getParticipant());
                            System.out.println(ConnectionManager.getContactByJID(chat.getChatXMPP().getParticipant()).getPresence());
                            Tab chatTab = chat.asChatTab();
                            ObservableList<Tab> tabs = tabConvList.getTabs();
                            tabs.add(chatTab);
                            tabConvList.getSelectionModel().select(chatTab);
                            event.consume();
                        } else {
                            tabConvList.getSelectionModel().select(ConnectionManager.getContactMap().get(ConnectionManager.getContactByJID(contact.getObjectModel().getEntry().getJid())).selectFirstChatTab());
                        }
                    }
                });
			}
		}
		try {
			VCard card = ConnectionManager.getConnectionList().get(0).getvCardManager().loadVCard(JidCreate.entityBareFrom(ConnectionManager.getConnectionList().get(0).getUser()));
			lblPseudo.setText(card.getNickName());
			System.out.println(card.getAvatarHash());

		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPException.XMPPErrorException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (XmppStringprepException e) {
			e.printStackTrace();
		}
		StatusRound.update(menuRoundStatus, StatusRound.Status.ONLINE);
		menuStatus.setText(LangManager.text("vMail.menu.status.online"));
	}

	public TabPane getTabConvList() {
		return tabConvList;
	}

	public Circle getMenuRoundStatus() {
		return menuRoundStatus;
	}
}

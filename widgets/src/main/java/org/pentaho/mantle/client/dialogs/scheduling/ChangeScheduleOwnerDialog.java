/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.mantle.client.dialogs.scheduling;

import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.environment.EnvironmentHelper;
import org.pentaho.mantle.client.messages.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChangeScheduleOwnerDialog extends PromptDialogBox {
  private static final String DIALOG_TITLE = Messages.getString( "schedule.changeOwner" );
  private static final String DIALOG_OK = Messages.getString( "ok" );
  private static final String DIALOG_CANCEL = Messages.getString( "cancel" );
  static final int MAX_DROP_VISIBLE = 5;

  private final TextBox ownerInput = new TextBox();
  private final CustomListBox newOwnerList = new CustomListBox();

  public ChangeScheduleOwnerDialog() {
    super( DIALOG_TITLE, DIALOG_OK, DIALOG_CANCEL, false, true );

    setResponsive( true );
    setSizingMode( DialogSizingMode.FILL_VIEWPORT );
    setWidthCategory( DialogWidthCategory.SMALL );

    createUI();
  }

  public TextBox getOwnerInput() {
    return this.ownerInput;
  }

  public String getOwner() {
    return getOwnerInput().getText();
  }

  public void setOwner( String owner ) {
    getOwnerInput().setText( owner );
  }

  public CustomListBox getNewOwnerList() {
    return this.newOwnerList;
  }

  public String getNewOwner() {
    return getNewOwnerList().getValue();
  }

  private void createUI() {
    VerticalPanel content = new VerticalFlexPanel();

    content.add( createOwnerUI() );
    content.add( createNewOwnerUI() );
    updateNewOwnersList();

    setContent( content );
    content.getElement().getStyle().clearHeight();
    content.getParent().setHeight( "100%" );
    content.getElement().getParentElement().getStyle().setVerticalAlign( Style.VerticalAlign.TOP );

    setSize( "650px", "450px" );
    addStyleName( "change-schedule-owner-dialog" );
  }

  /* Visible for testing */
  VerticalPanel createOwnerUI() {
    VerticalPanel panel = new VerticalFlexPanel();

    Label label = new Label( Messages.getString( "schedule.currentOwner" ) );
    label.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    panel.add( label );

    TextBox input = getOwnerInput();
    input.addStyleName( ScheduleEditor.SCHEDULE_INPUT );
    input.setEnabled( false );
    panel.add( input );

    return panel;
  }

  /* Visible for testing */
  VerticalPanel createNewOwnerUI() {
    VerticalPanel panel = new VerticalFlexPanel();

    Label newOwnerLabel = new Label( Messages.getString( "schedule.newOwner" ) );
    newOwnerLabel.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    panel.add( newOwnerLabel );

    CustomListBox list = getNewOwnerList();
    list.addStyleName( "schedule-custom-list" );
    list.setMaxDropVisible( MAX_DROP_VISIBLE );
    list.setDefaultSelectionEnabled( false );
    list.setSelectedItemPlaceholderText( Messages.getString( "schedule.selectUsername" ) );
    list.addChangeListener( event -> updateButtonState() );
    panel.add( list );

    return panel;
  }

  private void updateNewOwnersList() {
    final String userListUrl = EnvironmentHelper.getFullyQualifiedURL() + "api/userroledao/users";
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, userListUrl );

    try {
      RequestCallback rc = new RequestCallback() {
        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          if ( response.getStatusCode() == 200 ) {
            CustomListBox list = getNewOwnerList();

            List<String> items = parseUsersXml( response );
            items.forEach( list::addItem );

            list.setSearchable( items.size() > list.getMaxDropVisible() );
          }

          updateButtonState();
        }

        @Override
        public void onError( Request request, Throwable exception ) { /* noop */ }
      };
      builder.sendRequest( "", rc );

    } catch ( RequestException e ) {
      // noop
    }
  }

  private List<String> parseUsersXml( Response response ) {
    final List<String> users = new ArrayList<>();

    Document doc = XMLParser.parse( response.getText() );
    NodeList nodes = doc.getElementsByTagName( "users" );

    for ( int i = 0; i < nodes.getLength(); i++ ) {
      users.add( nodes.item( i ).getFirstChild().getNodeValue() );
    }
    Collections.sort( users );

    return users;
  }

  /* Visible for testing */
  Button getOkButton() {
    return this.okButton;
  }

  /* Visible for testing */
  void updateButtonState() {
    String newOwner = getNewOwner();

    boolean hasNewOwner = !StringUtils.isEmpty( newOwner );
    getOkButton().setEnabled( hasNewOwner && !getOwner().equals( newOwner ) );
  }
}

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
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

  private final TextBox ownerInput = new TextBox();
  private final CustomListBox newOwnerList = new CustomListBox();

  public ChangeScheduleOwnerDialog() {
    super( DIALOG_TITLE, DIALOG_OK, DIALOG_CANCEL, false, true );

    setResponsive( true );
    setSizingMode( DialogSizingMode.FILL_VIEWPORT );
    setWidthCategory( DialogWidthCategory.SMALL );

    createUI();
  }

  public void setOwner( String owner ) {
    this.ownerInput.setText( owner );
  }

  public String getOwner() {
    String newOwner = this.newOwnerList.getValue();
    if ( !StringUtils.isEmpty( newOwner) ) {
      return newOwner;
    }

    return this.ownerInput.getText();
  }

  private void createUI() {
    VerticalPanel content = new VerticalFlexPanel();
    content.addStyleName( "change-schedule-owner-content" );

    VerticalPanel currentOwnerPanel = new VerticalFlexPanel();
    content.add( currentOwnerPanel );

    Label ownerLabel = new Label( Messages.getString( "schedule.currentOwner" ) );
    ownerLabel.addStyleName( ScheduleEditor.SCHEDULE_LABEL );
    currentOwnerPanel.add( ownerLabel );

    ownerInput.addStyleName( "schedule-dialog-input" );
    ownerInput.setEnabled( false );
    currentOwnerPanel.add( ownerInput );

    VerticalPanel newOwnerPanel = new VerticalFlexPanel();
    content.add( newOwnerPanel );

    Label newOwnerLabel = new Label( Messages.getString( "schedule.newOwner" ) );
    newOwnerLabel.addStyleName( ScheduleEditor.SCHEDULE_LABEL );
    newOwnerPanel.add( newOwnerLabel );

    newOwnerList.addStyleName( "schedule-dialog-custom-list" );
    newOwnerList.setMaxDropVisible( 5 );
    newOwnerList.setDefaultSelectionEnabled( false );
    newOwnerList.setSelectedItemPlaceholder( Messages.getString( "schedule.selectUsername" ) );
    newOwnerList.addChangeListener( event -> updateButtonState() );
    updateNewOwnersList();
    newOwnerPanel.add( newOwnerList );

    setContent( content );
    content.getElement().getStyle().clearHeight();
    content.getParent().setHeight( "100%" );
    content.getElement().getParentElement().getStyle().setVerticalAlign( Style.VerticalAlign.TOP );

    setSize( "650px", "450px" );
    addStyleName( "change-schedule-owner-dialog" );
  }

  private void updateNewOwnersList() {
    final String userListUrl = EnvironmentHelper.getFullyQualifiedURL() + "api/userroledao/users";
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, userListUrl );

    try {
      RequestCallback rc = new RequestCallback() {
        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          if ( response.getStatusCode() == 200 ) {
            List<String> items = parseUsersXml( response );
            items.forEach( newOwnerList::addItem );

            newOwnerList.setSearchable( items.size() > newOwnerList.getMaxDropVisible() );
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

  private void updateButtonState() {
    okButton.setEnabled( !getOwner().equals( ownerInput.getText() ) );
  }

}

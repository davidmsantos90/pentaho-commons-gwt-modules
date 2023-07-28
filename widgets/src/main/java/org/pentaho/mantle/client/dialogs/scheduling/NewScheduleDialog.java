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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.mantle.client.dialogs.scheduling;

import java.util.Date;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.formatter.JSDateTextFormatter;
import org.pentaho.gwt.widgets.client.panel.HorizontalFlexPanel;
import org.pentaho.gwt.widgets.client.panel.VerticalFlexPanel;
import org.pentaho.gwt.widgets.client.utils.ImageUtil;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog;
import org.pentaho.gwt.widgets.client.wizards.AbstractWizardDialog.ScheduleDialogType;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.dialogs.folderchooser.SelectFolderDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.environment.EnvironmentHelper;
import org.pentaho.mantle.client.workspace.JsJob;
import org.pentaho.mantle.client.workspace.JsJobParam;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class NewScheduleDialog extends PromptDialogBox {
  private static final String DIALOG_NEW_TITLE = Messages.getString( "newSchedule" );
  private static final String DIALOG_EDIT_TITLE = Messages.getString( "editSchedule" );
  private static final String DIALOG_NEXT = Messages.getString( "next" );
  private static final String DIALOG_CANCEL = Messages.getString( "cancel" );

  private String filePath;
  private IDialogCallback callback;
  private boolean isEmailConfValid;
  private JsJob jsJob;
  private ScheduleRecurrenceDialog recurrenceDialog = null;

  private final TextBox scheduleNameTextBox = new TextBox();
  private final ListBox timestampLB = new ListBox();
  private final CheckBox appendTimeChk = new CheckBox();
  private final CaptionPanel previewCaptionPanel = new CaptionPanel( Messages.getString( "preview" ) );
  private final Label scheduleNamePreviewLabel = new Label();

  private final TextBox scheduleLocationTextBox = new TextBox();
  private final Button selectLocationButton = new Button( Messages.getString( "select" ) );
  private final CheckBox overrideExistingChk = new CheckBox();

  private final TextBox scheduleOwnerTextBox = new TextBox();
  private final Button changeOwnerButton = new Button( Messages.getString( "change" ) );
  private final HorizontalPanel scheduleOwnerErrorPanel = new HorizontalFlexPanel();

  private static native String getDefaultSaveLocation()
  /*-{
      return window.top.HOME_FOLDER;
  }-*/;

  private static native void delete( JsArray<?> array, int index, int count )
  /*-{
      array.splice(index, count);
  }-*/;

  /**
   * @deprecated Need to set callback
   */
  public NewScheduleDialog( JsJob jsJob, IDialogCallback callback, boolean isEmailConfValid ) {
    super( DIALOG_EDIT_TITLE, DIALOG_NEXT, DIALOG_CANCEL, false, true );

    this.jsJob = jsJob;
    this.filePath = jsJob.getFullResourceName();
    this.callback = callback;
    this.isEmailConfValid = isEmailConfValid;

    createUI();

    setResponsive( true );
    setSizingMode( DialogSizingMode.FILL_VIEWPORT );
    setWidthCategory( DialogWidthCategory.SMALL );
  }

  public NewScheduleDialog( String filePath, IDialogCallback callback, boolean isEmailConfValid ) {
    super( DIALOG_NEW_TITLE, DIALOG_NEXT, DIALOG_CANCEL, false, true );

    this.filePath = filePath;
    this.callback = callback;
    this.isEmailConfValid = isEmailConfValid;

    createUI();

    setResponsive( true );
    setSizingMode( DialogSizingMode.FILL_VIEWPORT );
    setWidthCategory( DialogWidthCategory.SMALL );
  }

  // region getters/setters
  public TextBox getScheduleNameTextBox() {
    return this.scheduleNameTextBox;
  }

  public ListBox getTimestampListBox() {
    return this.timestampLB;
  }

  public CheckBox getAppendTimeCheckbox() {
    return this.appendTimeChk;
  }

  public CaptionPanel getPreviewCaptionPanel() {
    return this.previewCaptionPanel;
  }

  public Label getScheduleNamePreviewLabel() {
    return this.scheduleNamePreviewLabel;
  }

  public TextBox getScheduleLocationTextBox() {
    return this.scheduleLocationTextBox;
  }

  public Button getSelectLocationButton() {
    return this.selectLocationButton;
  }

  public CheckBox getOverrideExistingCheckbox() {
    return this.overrideExistingChk;
  }

  public TextBox getScheduleOwnerTextBox() {
    return this.scheduleOwnerTextBox;
  }

  public HorizontalPanel getScheduleOwnerErrorPanel() {
    return this.scheduleOwnerErrorPanel;
  }

  public Button getChangeOwnerButton() {
    return this.changeOwnerButton;
  }
  // endregion getters / setters

  private void createUI() {
    VerticalFlexPanel content = new VerticalFlexPanel();

    content.add( createScheduleNameUI() );
    updateScheduleNamePanel();

    content.add( createScheduleLocationUI() );
    updateScheduleLocationPanel();

    content.add( createScheduleOwnerUI() );
    updateScheduleOwnerTextBox();
    updateChangeOwnerButton();

    refreshAppendedTimestamp( appendTimeChk.getValue() );
    validateScheduleLocationTextBox();

    setContent( content );
    content.getElement().getStyle().clearHeight();
    content.getParent().setHeight( "100%" );
    content.getElement().getParentElement().getStyle().setVerticalAlign( VerticalAlign.TOP );

    okButton.getParent().getParent().addStyleName( "button-panel" );

    setSize( "650px", "450px" );
    addStyleName( "new-schedule-dialog" );
  }

  /* Visible for testing */
  VerticalPanel createScheduleNameUI() {
    VerticalPanel panel = new VerticalFlexPanel();

    HorizontalFlexPanel labelPanel = new HorizontalFlexPanel();
    panel.add( labelPanel );

    Label scheduleNameLabel = new Label( Messages.getString( "scheduleNameColon" ) );
    scheduleNameLabel.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    scheduleNameLabel.addStyleName( "schedule-name" );
    labelPanel.add( scheduleNameLabel );

    Label scheduleNameInfoLabel = new Label( Messages.getString( "scheduleNameInfo" ) );
    scheduleNameInfoLabel.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    scheduleNameInfoLabel.addStyleName( "msg-Label" );
    scheduleNameInfoLabel.addStyleName( "schedule-name-info" );
    labelPanel.add( scheduleNameInfoLabel );

    HorizontalFlexPanel scheduleNamePanel = new HorizontalFlexPanel();
    scheduleNamePanel.addStyleName( "schedule-name-panel" );
    panel.add( scheduleNamePanel );

    TextBox scheduleNameInput = getScheduleNameTextBox();
    scheduleNameInput.addStyleName( ScheduleEditor.SCHEDULE_INPUT );
    scheduleNameInput.addKeyUpHandler( event -> onScheduleChangeHandler() );
    scheduleNameInput.addChangeHandler( event -> onScheduleChangeHandler() );
    scheduleNamePanel.add( scheduleNameInput );

    ListBox timestampList = getTimestampListBox();
    timestampList.addItem( "yyyy-MM-dd" );
    timestampList.addItem( "yyyyMMdd" );
    timestampList.addItem( "yyyyMMddHHmmss" );
    timestampList.addItem( "MM-dd-yyyy" );
    timestampList.addItem( "MM-dd-yy" );
    timestampList.addItem( "dd-MM-yyyy" );

    timestampList.addStyleName( "schedule-timestamp-listbox" );
    timestampList.setVisible( false );
    timestampList.addChangeHandler( event -> {
      int index = ( (ListBox) event.getSource() ).getSelectedIndex();
      getScheduleNamePreviewLabel().setText( getPreviewName( index ) );
    } );
    scheduleNamePanel.add( timestampList );

    CheckBox appendTimeCheckbox = getAppendTimeCheckbox();
    appendTimeCheckbox.setText( Messages.getString( "appendTimeToName" ) );
    appendTimeCheckbox.addClickHandler( event -> {
      boolean checked = ( (CheckBox) event.getSource() ).getValue();
      refreshAppendedTimestamp( checked );
    });
    panel.add( appendTimeCheckbox );

    CaptionPanel previewPanel = getPreviewCaptionPanel();
    previewPanel.setStyleName( "schedule-caption-panel" );
    previewPanel.setVisible( false );
    panel.add( previewPanel );

    Label previewLabel = getScheduleNamePreviewLabel();
    previewLabel.addStyleName( "schedule-name-preview" );
    previewPanel.add( previewLabel );

    return panel;
  }

  /* Visible for testing */
  VerticalPanel createScheduleLocationUI() {
    VerticalPanel panel = new VerticalFlexPanel();

    Label scheduleLocationLabel = new Label( Messages.getString( "generatedContentLocation" ) );
    scheduleLocationLabel.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    panel.add( scheduleLocationLabel );

    HorizontalFlexPanel locationPanel = new HorizontalFlexPanel();
    panel.add( locationPanel );

    TextBox scheduleLocationInput = getScheduleLocationTextBox();
    scheduleLocationInput.addStyleName( ScheduleEditor.SCHEDULE_INPUT );
    scheduleLocationInput.setEnabled( false );
    scheduleLocationInput.addChangeHandler( event -> onScheduleChangeHandler() );
    locationPanel.add( scheduleLocationInput );

    Button browseButton = getSelectLocationButton();
    browseButton.setStyleName( AbstractWizardDialog.PENTAHO_BUTTON );
    browseButton.addStyleName( ScheduleEditor.SCHEDULE_BUTTON );
    browseButton.addClickHandler( event -> showSelectFolderDialog() );
    locationPanel.add( browseButton );

    panel.add( getOverrideExistingCheckbox() );

    return panel;
  }

  /* Visible for testing */
  VerticalPanel createScheduleOwnerUI() {
    VerticalPanel panel = new VerticalFlexPanel();

    Label label = new Label( Messages.getString( "owner" ) );
    label.setStyleName( ScheduleEditor.SCHEDULE_LABEL );
    panel.add( label );

    HorizontalPanel ownerPanel = new HorizontalFlexPanel();
    panel.add( ownerPanel );

    VerticalPanel inputPanel = new VerticalFlexPanel();
    inputPanel.addStyleName( "with-layout-gap-none" );
    ownerPanel.add( inputPanel );

    TextBox ownerInput = getScheduleOwnerTextBox();
    ownerInput.addStyleName( ScheduleEditor.SCHEDULE_INPUT );
    ownerInput.setEnabled( false );
    inputPanel.add( ownerInput );

    HorizontalPanel ownerErrorPanel = getScheduleOwnerErrorPanel();
    ownerErrorPanel.setVisible( false );
    inputPanel.add( ownerErrorPanel );

    ownerErrorPanel.add( ImageUtil.getThemeableImage( "pentaho-error-button", "icon-zoomable" ) );

    Label errorLabel = new Label( Messages.getString( "schedule.invalidOwner" ) );
    errorLabel.addStyleName( "schedule-error" );
    ownerErrorPanel.add( errorLabel );

    Button changeOwner = getChangeOwnerButton();
    changeOwner.setStyleName( AbstractWizardDialog.PENTAHO_BUTTON );
    changeOwner.addStyleName( ScheduleEditor.SCHEDULE_BUTTON );
    changeOwner.addClickHandler( event -> showChangeOwnerDialog() );
    ownerPanel.add( changeOwner );

    return panel;
  }

  /* Visible for testing */
  void onScheduleChangeHandler() {
    scheduleNamePreviewLabel.setText( getPreviewName( timestampLB.getSelectedIndex() ) );
    updateButtonState();
  }

  private void updateScheduleNamePanel() {
    if ( jsJob != null ) {
      setScheduleName( jsJob.getJobName() );

      String appendDateFormat = jsJob.getJobParamValue( ScheduleParamsHelper.APPEND_DATE_FORMAT_KEY );
      if ( appendDateFormat != null ) {
        appendTimeChk.setValue( true );
        for ( int i = 0; i < timestampLB.getItemCount(); i++ ) {
          if ( appendDateFormat.equals( timestampLB.getValue( i ) ) ) {
            timestampLB.setSelectedIndex( i );
            break;
          }
        }
      }

      return;
    }

    String defaultName = filePath.substring( filePath.lastIndexOf( "/" ) + 1, filePath.lastIndexOf( "." ) );
    setScheduleName( defaultName );
  }

  private void updateScheduleLocationPanel() {
    if ( jsJob != null ) {
      scheduleLocationTextBox.setText( jsJob.getOutputPath() );

      String autoCreateUniqueFilename = jsJob.getJobParamValue( ScheduleParamsHelper.AUTO_CREATE_UNIQUE_FILENAME_KEY );
      if ( autoCreateUniqueFilename != null ) {
        boolean autoCreate = Boolean.parseBoolean( autoCreateUniqueFilename );
        if ( !autoCreate ) {
          overrideExistingChk.setValue( true );
        }
      }

      return;
    }

    scheduleLocationTextBox.setText( getDefaultSaveLocation() );
  }

  private void updateScheduleOwnerTextBox() {
    if ( jsJob != null ) {
      scheduleOwnerTextBox.setText( jsJob.getUserName() );
      validateScheduleOwner();
      return;
    }

    final String currentUserUrl = EnvironmentHelper.getFullyQualifiedURL() + "api/session/userName";
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, currentUserUrl );

    try {
      RequestCallback rc = new RequestCallback() {
        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          if ( response.getStatusCode() == 200 ) {
            scheduleOwnerErrorPanel.setVisible( false );
            scheduleOwnerTextBox.setText( response.getText() );
            updateButtonState();
          }
        }

        @Override
        public void onError( Request request, Throwable exception ) { /* noop */ }
      };
      builder.sendRequest( "", rc );

    } catch ( RequestException e ) {
      // noop
    }
  }

  private void updateChangeOwnerButton() {
    if ( jsJob == null ) {
      changeOwnerButton.setEnabled( false );
      return;
    }

    final String isAdminUserUrl = EnvironmentHelper.getFullyQualifiedURL() + "api/mantle/isAdministrator";
    final RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, isAdminUserUrl );

    try {
      RequestCallback rc = new RequestCallback() {
        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          if ( response.getStatusCode() == 200 ) {
            boolean isAdmin = Boolean.parseBoolean(response.getText());
            changeOwnerButton.setEnabled( isAdmin );
          }
        }

        @Override
        public void onError( Request request, Throwable exception ) { /* noop */ }
      };
      builder.sendRequest( "", rc );

    } catch ( RequestException e ) {
      // noop
    }
  }

  private void showSelectFolderDialog() {
    final SelectFolderDialog selectFolder = new SelectFolderDialog();

    selectFolder.setCallback( new IDialogCallback() {
      public void okPressed() {
        getScheduleLocationTextBox().setText( selectFolder.getSelectedPath() );
      }

      public void cancelPressed() { /* noop */ }
    } );
    selectFolder.center();
  }

  private void showChangeOwnerDialog() {
    final ChangeScheduleOwnerDialog dialog = new ChangeScheduleOwnerDialog();
    dialog.setOwner( getScheduleOwnerTextBox().getText() );

    dialog.setCallback( new IDialogCallback() {
      public void okPressed() {
        scheduleOwnerTextBox.setText( dialog.getNewOwner() );
        scheduleOwnerErrorPanel.setVisible( false );

        updateButtonState();
      }

      public void cancelPressed() { /* noop */ }
    } );
    dialog.center();
  }

  protected void onOk() {
    String name;
    if ( appendTimeChk.getValue() ) {
      name = getPreviewName( timestampLB.getSelectedIndex() );
    } else {
      //trim the name if there is no timestamp appended
      scheduleNameTextBox.setText( getScheduleName().trim() );

      name = getScheduleName();
    }

    if ( !NameUtils.isValidFileName( name ) ) {
      MessageDialogBox errorDialog =
          new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "prohibitedNameSymbols", name,
              NameUtils.reservedCharListForDisplay( " " ) ), false, false, true );
      errorDialog.center();
      return;
    }

    // check if has parameterizable
    WaitPopup.getInstance().setVisible( true );
    String urlPath = URL.encodePathSegment( NameUtils.encodeRepositoryPath( filePath ) );

    RequestBuilder scheduleFileRequestBuilder;
    final boolean isXAction;

    if ( ( urlPath != null ) && ( urlPath.endsWith( "xaction" ) ) ) {
      isXAction = true;
      scheduleFileRequestBuilder = new RequestBuilder( RequestBuilder.GET, EnvironmentHelper.getFullyQualifiedURL() + "api/repos/" + urlPath
          + "/parameterUi" );
    } else {
      isXAction = false;
      scheduleFileRequestBuilder = new RequestBuilder( RequestBuilder.GET, EnvironmentHelper.getFullyQualifiedURL() + "api/repo/files/" + urlPath
          + "/parameterizable" );
    }

    scheduleFileRequestBuilder.setHeader( "accept", "text/plain" );
    scheduleFileRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      scheduleFileRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          WaitPopup.getInstance().setVisible( false );
          MessageDialogBox dialogBox =
            new MessageDialogBox( Messages.getString( "error" ), exception.toString(), false, false, true );
          dialogBox.center();
        }

        public void onResponseReceived( Request request, Response response ) {
          WaitPopup.getInstance().setVisible( false );
          if ( response.getStatusCode() == Response.SC_OK ) {
            String responseMessage = response.getText();
            boolean hasParams;

            if ( isXAction ) {
              int numOfInputs = StringUtils.countMatches( responseMessage, "<input" );
              int NumOfHiddenInputs = StringUtils.countMatches( responseMessage, "type=\"hidden\"" );
              hasParams = numOfInputs - NumOfHiddenInputs > 0;
            } else {
              hasParams = Boolean.parseBoolean( response.getText() );
            }

            boolean overwriteFile = overrideExistingChk.getValue();
            String dateFormat = null;
            if ( appendTimeChk.getValue() ) {
              dateFormat = timestampLB.getValue( timestampLB.getSelectedIndex() );
            }

            if ( jsJob != null ) {
              jsJob.setJobName( getScheduleName() );
              jsJob.setOutputPath( getScheduleLocation(), getScheduleName() );

              String scheduleOwner = scheduleOwnerTextBox.getText().trim();
              if ( !StringUtils.isEmpty( scheduleOwner ) && !scheduleOwner.equalsIgnoreCase( jsJob.getUserName() ) ) {
                JsJobParam actionUser = jsJob.getJobParam( ScheduleParamsHelper.ACTION_USER_KEY );

                if ( actionUser != null ) {
                  if ( !scheduleOwner.equalsIgnoreCase( actionUser.getValue() ) ) {
                    actionUser.setValue( scheduleOwner );
                  }
                } else {
                  JsJobParam jjp = JavaScriptObject.createObject().cast();
                  jjp.setName( ScheduleParamsHelper.ACTION_USER_KEY );
                  jjp.setValue( scheduleOwner );
                  jsJob.getJobParams().push( jjp );
                }
              }

              if ( jsJob.getJobParamValue( ScheduleParamsHelper.APPEND_DATE_FORMAT_KEY ) != null ) {
                if ( dateFormat != null ) {
                  JsJobParam jp = jsJob.getJobParam( ScheduleParamsHelper.APPEND_DATE_FORMAT_KEY );
                  jp.setValue( dateFormat );
                } else {
                  for ( int j = 0; j < jsJob.getJobParams().length(); j++ ) {
                    JsJobParam jjp = jsJob.getJobParams().get( j );
                    if ( ScheduleParamsHelper.APPEND_DATE_FORMAT_KEY.equals( jjp.getName() ) ) {
                      delete( jsJob.getJobParams(), j, 1 );
                    }
                  }
                }
              } else {
                if ( dateFormat != null ) {
                  JsJobParam jjp = JavaScriptObject.createObject().cast();
                  jjp.setName( ScheduleParamsHelper.APPEND_DATE_FORMAT_KEY );
                  jjp.setValue( dateFormat );
                  jsJob.getJobParams().set( jsJob.getJobParams().length(), jjp );
                }
              }

              if ( jsJob.getJobParamValue( ScheduleParamsHelper.AUTO_CREATE_UNIQUE_FILENAME_KEY ) != null ) {
                if ( !jsJob.getJobParamValue( ScheduleParamsHelper.AUTO_CREATE_UNIQUE_FILENAME_KEY )
                  .equals(String.valueOf( !overwriteFile ) ) ) {
                  JsJobParam jp = jsJob.getJobParam( ScheduleParamsHelper.AUTO_CREATE_UNIQUE_FILENAME_KEY );
                  jp.setValue( String.valueOf( !overwriteFile ) );
                }
              } else {
                JsJobParam jjp = JavaScriptObject.createObject().cast();
                jjp.setName( ScheduleParamsHelper.AUTO_CREATE_UNIQUE_FILENAME_KEY );
                jjp.setValue( String.valueOf( !overwriteFile ) );
                jsJob.getJobParams().set( jsJob.getJobParams().length(), jjp );
              }

              if ( recurrenceDialog == null ) {
                recurrenceDialog = new ScheduleRecurrenceDialog( NewScheduleDialog.this, jsJob, callback,
                  hasParams, isEmailConfValid, ScheduleDialogType.SCHEDULER );
              }
            } else if ( recurrenceDialog == null ) {
              recurrenceDialog = new ScheduleRecurrenceDialog( NewScheduleDialog.this, filePath,
                getScheduleLocation(), getScheduleName(), dateFormat, overwriteFile, callback,
                hasParams, isEmailConfValid );
            } else {
              recurrenceDialog.scheduleName = getScheduleName();
              recurrenceDialog.outputLocation = getScheduleLocation();
            }

            recurrenceDialog.setParentDialog( NewScheduleDialog.this );
            recurrenceDialog.center();
            NewScheduleDialog.super.onOk();
          }
        }
      } );
    } catch ( RequestException e ) {
      WaitPopup.getInstance().setVisible( false );
      // showError(e);
    }
  }


  /* Visible for testing */
  Button getOkButton() {
    return this.okButton;
  }

  /* Visible for testing */
  void updateButtonState() {
    boolean hasLocation = !StringUtils.isEmpty( getScheduleLocation() );
    boolean hasName = !StringUtils.isEmpty( getScheduleName() );
    boolean hasOwner = !StringUtils.isEmpty( getScheduleOwnerTextBox().getText() );
    boolean hasOwnerError = getScheduleOwnerErrorPanel().isVisible();

    getOkButton().setEnabled( hasLocation && hasName && hasOwner && !hasOwnerError );
  }

  public void setFocus() {
    scheduleNameTextBox.setFocus( true );
  }

  public String getScheduleName() {
    return scheduleNameTextBox.getText();
  }

  public String getScheduleLocation() {
    return scheduleLocationTextBox.getText();
  }

  public String getPreviewName( int index ) {
    JSDateTextFormatter formatter = new JSDateTextFormatter( timestampLB.getValue( index ) );
    Date date = new Date();
    return getScheduleName() + formatter.format( String.valueOf( date.getTime() ) );
  }

  public void setScheduleName( String scheduleName ) {
    scheduleNameTextBox.setText( scheduleName );
  }

  private void validateScheduleLocationTextBox() {
    final Command errorCallback = () -> {
      String previousPath = OutputLocationUtils.getPreviousLocationPath( getScheduleLocation() );
      if ( !previousPath.isEmpty() ) {
        scheduleLocationTextBox.setText( previousPath );
        validateScheduleLocationTextBox();
      } else {
        scheduleLocationTextBox.setText( getDefaultSaveLocation() ); // restore default location
      }

      updateButtonState();
    };

    OutputLocationUtils.validateOutputLocation( getScheduleLocation(), null, errorCallback );
  }

  private void validateScheduleOwner() {
    String owner = scheduleOwnerTextBox.getText();

    String currentUserUrl = EnvironmentHelper.getFullyQualifiedURL() + "api/userrolelist/getRolesForUser?user=" + owner;
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, currentUserUrl );

    try {
      RequestCallback requestCallback = new RequestCallback() {
        @Override
        public void onResponseReceived( final Request request, final Response response ) {
          boolean showError = false;
          if ( response.getStatusCode() == 200 ) {
            // all users have at least one role (Authenticated or Anonymous),
            // so if no roles are returned, the user doesn't exist.
            showError = StringUtils.countMatches( response.getText(), "<role>" ) == 0;
          }

          scheduleOwnerErrorPanel.setVisible( showError );
          updateButtonState();
        }

        @Override
        public void onError( Request request, Throwable exception ) { /* noop */ }
      };
      builder.sendRequest( "", requestCallback );

    } catch ( RequestException e ) {
      // noop
    }
  }

  /**
   * Refresh Appended Timestamp
   *
   * Refresh the New Schedule UI to update multiple components that change based on whether the timestamp is appended
   * to the schedule name.
   *
   * @param value - true if the timestamp should be appended, otherwise false
   */
  private void refreshAppendedTimestamp( boolean value ) {
    getPreviewCaptionPanel().setVisible( value );
    getTimestampListBox().setVisible( value );
    if ( value ) {
      getOverrideExistingCheckbox().setText( Messages.getString( "overrideExistingFileAndTime" ) );

      // Update the preview text
      getScheduleNamePreviewLabel().setText( getPreviewName( timestampLB.getSelectedIndex() ) );
    } else {
      getOverrideExistingCheckbox().setText( Messages.getString( "overrideExistingFile" ) );
    }
  }
}

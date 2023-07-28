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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.gwt.widgets.client.listbox.CustomListBox;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.mantle.client.dialogs.scheduling.ChangeScheduleOwnerDialog.MAX_DROP_VISIBLE;

@SuppressWarnings( "deprecation" )
@RunWith( GwtMockitoTestRunner.class )
public class ChangeScheduleOwnerDialogTest {

  private ChangeScheduleOwnerDialog dialog;

  @Mock
  private TextBox ownerInput;
  @Mock
  private CustomListBox newOwnerList;
  @Mock
  private Button okButton;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks( this );
    dialog = mock( ChangeScheduleOwnerDialog.class );

    when( dialog.getOkButton() ).thenReturn( okButton );
    when( dialog.getOwnerInput() ).thenReturn( ownerInput );
    when( dialog.getNewOwnerList() ).thenReturn( newOwnerList );
  }

  @Test
  public void testCreateOwnerUI() {
    doCallRealMethod().when( dialog ).createOwnerUI();
    dialog.createOwnerUI();

    verify( ownerInput ).addStyleName( ScheduleEditor.SCHEDULE_INPUT );
    verify( ownerInput ).setEnabled( false );
  }

  @Test
  public void testCreateNewOwnerUI() {
    doCallRealMethod().when( dialog ).createNewOwnerUI();
    dialog.createNewOwnerUI();

    verify( newOwnerList ).addStyleName( "schedule-custom-list" );
    verify( newOwnerList ).setMaxDropVisible( MAX_DROP_VISIBLE );
    verify( newOwnerList ).setSelectedItemPlaceholderText( "schedule.selectUsername" );
    verify( newOwnerList ).setDefaultSelectionEnabled( false );
    verify( newOwnerList ).addChangeListener( any( ChangeListener.class ) );
  }

  @Test
  public void testUpdateButtonState_newOwnerNotSelected() {
    doCallRealMethod().when( dialog ).updateButtonState();
    when( dialog.getOwner() ).thenReturn( "owner" );
    when( dialog.getNewOwner() ).thenReturn( null );

    dialog.updateButtonState();

    verify( dialog.getOkButton() ).setEnabled( false );
  }

  @Test
  public void testUpdateButtonState_newOwnerSelected() {
    doCallRealMethod().when( dialog ).updateButtonState();
    when( dialog.getOwner() ).thenReturn( "owner" );
    when( dialog.getNewOwner() ).thenReturn( "new-owner" );

    dialog.updateButtonState();

    verify( dialog.getOkButton() ).setEnabled( true );
  }
}

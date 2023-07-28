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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.mantle.client.workspace.JsJob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( GwtMockitoTestRunner.class )
public class ScheduleParamsDialogTest {
  @Mock
  private ScheduleParamsDialog dialog;
  @Mock
  private ScheduleParamsWizardPanel scheduleParamsWizardPanel;
  @Mock
  JSONObject jobSchedule;
  @Mock
  private JSONArray scheduleParams;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks( this );

    dialog.jobSchedule = jobSchedule;
    dialog.scheduleParamsWizardPanel = scheduleParamsWizardPanel;
    when( dialog.getScheduleParams( false ) ).thenReturn( scheduleParams );
  }

  @Test
  public void testOnKeyDownPreview() {
    doCallRealMethod().when( dialog ).onKeyDownPreview( anyChar(), anyInt() );

    assertTrue( dialog.onKeyDownPreview( (char) KeyCodes.KEY_ENTER, -1 ) );
    verify( dialog, never() ).hide();

    assertTrue( dialog.onKeyDownPreview( (char) KeyCodes.KEY_ESCAPE, -1 ) );
    verify( dialog).hide();
  }

  @Test
  public void testBackClicked() {
    doCallRealMethod().when( dialog ).backClicked();

    dialog.parentDialog = mock( ScheduleRecurrenceDialog.class );
    final JSONArray jsonArray = mock( JSONArray.class );
    when( dialog.getScheduleParams( true ) ).thenReturn( jsonArray );

    dialog.backClicked();

    assertEquals( jsonArray, dialog.scheduleParams );
    verify( dialog.parentDialog ).center();
    verify( dialog ).hide();
  }

  @Test
  public void getScheduleParamsWithoutScheduleParams() {
    doCallRealMethod().when( dialog ).getScheduleParams( true );

    when( scheduleParamsWizardPanel.getParams( true ) ).thenReturn( mock( JsArray.class ) );

    JSONArray params = dialog.getScheduleParams( true );
    assertEquals( 0, params.size() );
  }

  @Test
  public void testOnFinish_editExistingSchedule() {
    doCallRealMethod().when( dialog ).onFinish();

    JSONObject scheduleRequest = mock( JSONObject.class );
    when( dialog.parseStrictScheduleJob() ).thenReturn( scheduleRequest );

    JSONObject actionUser = mock( JSONObject.class );
    when( dialog.generateActionUser() ).thenReturn( actionUser );
    JSONObject lineageId = mock( JSONObject.class );
    when( dialog.generateLineageId() ).thenReturn( lineageId );

    dialog.editJob =  mock( JsJob.class );
    dialog.onFinish();

    verify( scheduleParams ).set( anyInt(), eq( actionUser ) );
    verify( scheduleParams ).set( anyInt(), eq( lineageId ) );
    verify( scheduleRequest ).put( ScheduleParamsHelper.JOB_PARAMETERS_KEY, scheduleParams );
  }

  @Test
  public void testOnFinish_newSchedule() {
    doCallRealMethod().when( dialog ).onFinish();

    JSONObject scheduleRequest = mock( JSONObject.class );
    when( dialog.parseStrictScheduleJob() ).thenReturn( scheduleRequest );

    JSONObject actionUser = mock( JSONObject.class );
    when( dialog.generateActionUser() ).thenReturn( actionUser );
    JSONObject lineageId = mock( JSONObject.class );
    when( dialog.generateLineageId() ).thenReturn( lineageId );

    dialog.editJob = null;
    dialog.onFinish();

    verify( scheduleParams, never() ).set( anyInt(), eq( actionUser ) );
    verify( scheduleParams, never() ).set( anyInt(), eq( lineageId ) );
    verify( scheduleRequest ).put( ScheduleParamsHelper.JOB_PARAMETERS_KEY, scheduleParams );
  }
}

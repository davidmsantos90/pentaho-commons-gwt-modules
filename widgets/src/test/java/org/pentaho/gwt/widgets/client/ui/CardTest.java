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
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.gwt.widgets.client.ui;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.gwt.widgets.client.ui.Card.EMPTY_DESCRIPTION;

@RunWith( GwtMockitoTestRunner.class )
public class CardTest {
  private final String mockTitle = "test mock title";
  @Mock
  private Widget mockWidget;

  private Card card;

  @Mock
  private HorizontalPanel headerPanel;

  @Mock
  private VerticalPanel headerTitlePanel;

  @Mock
  private Label headerTitle;

  @Mock
  private HTML headerDescription;

  @Mock
  private Image headerIcon;

  @Mock
  private HorizontalPanel contentPanel;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void testCreateUI() {
    String status = "mock-status";
    String subtitle = "test mock subtitle";
    setupCardSpy( new Card( status, mockTitle, subtitle, mockWidget ) );

    card.createUI();

    verifyHeaderUI( status, subtitle );
    verifyContentUI();
  }

  @Test
  public void testCreateUI_nullStatus() {
    String mockDescription = "test mock subtitle";
    setupCardSpy( new Card( mockTitle, mockDescription, mockWidget ) );

    card.createUI();

    verifyHeaderUI( null, mockDescription );
    verifyContentUI();
  }


  @Test
  public void testCreateUI_nullSubtitle() {
    String status = "mock-status";
    setupCardSpy( new Card( status, mockTitle, null, mockWidget ) );

    card.createUI();

    verifyHeaderUI( status, null );
    verifyContentUI();
  }

  private void verifyHeaderUI( String status, String subtitle ) {
    verify( headerTitle ).setText( mockTitle );
    verify( headerTitle ).setStyleName( "typography typography-label" );
    verify( headerTitlePanel ).add( headerTitle );

    if ( subtitle == null ) {
      verify( headerDescription, never() ).setText( EMPTY_DESCRIPTION );
      verify( headerDescription ).setHTML( EMPTY_DESCRIPTION );
    } else {
      verify( headerDescription ).setText( subtitle );
      verify( headerDescription, never() ).setHTML( subtitle );
    }
    verify( headerDescription ).setStyleName( "typography typography-caption-1" );
    verify( headerTitlePanel ).add( headerDescription );

    verify( headerPanel ).addStyleName( "gwt-card-header" );
    verify( headerPanel ).add( headerTitlePanel );

    if ( status == null ) {
      verify( headerPanel, never() ).add( headerIcon );
    } else {
      verify( headerIcon ).addStyleName( "pentaho-status-" + status + "-button" );
      verify( headerPanel ).add( headerIcon );
    }

    verify( card ).add( headerPanel );
  }

  private void verifyContentUI() {
    verify( contentPanel ).addStyleName( "gwt-card-content" );
    verify( contentPanel ).add( mockWidget );

    verify( card ).add( contentPanel );
  }

  private void setupCardSpy( Card card ) {
    this.card = spy( card );

    when( this.card.getHeaderPanel() ).thenReturn( headerPanel );
    when( this.card.getHeaderTitlePanel() ).thenReturn( headerTitlePanel );
    when( this.card.getHeaderTitle() ).thenReturn( headerTitle );
    when( this.card.getHeaderSubtitle() ).thenReturn( headerDescription );
    when( this.card.getHeaderIcon() ).thenReturn( headerIcon );
    when( this.card.getContentPanel() ).thenReturn( contentPanel );
  }
}

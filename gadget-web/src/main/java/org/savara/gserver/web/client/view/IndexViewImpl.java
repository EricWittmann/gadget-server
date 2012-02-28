/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.gserver.web.client.view;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.savara.gserver.web.client.ApplicationEntryPoint;
import org.savara.gserver.web.client.presenter.IndexPresenter;

/**
 * @author: Jeff Yu
 * @date: 20/02/12
 */
public class IndexViewImpl extends ViewImpl implements IndexPresenter.IndexView {

    private LayoutPanel headerPanel;
    private LayoutPanel mainContentPanel;
    private LayoutPanel footerPanel;

    private DockLayoutPanel panel;

    @Inject
    public IndexViewImpl() {
        mainContentPanel = new LayoutPanel();
        mainContentPanel.setStyleName("main-content-panel");

        headerPanel = new LayoutPanel();
        headerPanel.setStyleName("header-panel");

        footerPanel = new LayoutPanel();
        footerPanel.setStyleName("footer-panel");

        panel = new DockLayoutPanel(Style.Unit.PX);
        panel.getElement().setAttribute("id", "container");

        panel.addNorth(headerPanel, 110);
        panel.addSouth(footerPanel, 25);
        panel.add(mainContentPanel);

        getHeaderPanel().add(ApplicationEntryPoint.MODULES.getHeader().asWidget());
        getFooterPanel().add(ApplicationEntryPoint.MODULES.getFooter().asWidget());

    }

    public Widget asWidget() {
        return panel;
    }

    public LayoutPanel getHeaderPanel() {
        return headerPanel;
    }

    public LayoutPanel getFooterPanel() {
        return footerPanel;
    }

}

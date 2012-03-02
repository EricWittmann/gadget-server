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
package org.savara.gserver.web.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jeff Yu
 * @date: 28/02/12
 */
public class Portal extends Composite {

    private int m_column;

    private FlowPanel portalPanel;
    
    private List<FlowPanel> columnPanel;

    public Portal() {
        portalPanel = new FlowPanel();
        portalPanel.getElement().addClassName("portal");
        initWidget(portalPanel);
    }

    public Portal(int column) {
        this();
        this.m_column = column;
        columnPanel = new ArrayList<FlowPanel>(column);
        
        for (int i = 0; i < column; i++) {
           FlowPanel cpanel = new FlowPanel();
           cpanel.getElement().addClassName("column");
           columnPanel.add(cpanel);
           portalPanel.add(cpanel);
        }
    }


    public void addPortlet(int i, Widget portlet) {
        columnPanel.get(i).add(portlet);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        sortableScript();
    }

    /**
     * JSNI methods
     */
    private static native void sortableScript() /*-{
        $wnd.$(".column").sortable({
            connectWith: ".column"
        });
    }-*/;
}

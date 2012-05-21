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
package org.savara.gadget.web.client.widgets;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.savara.gadget.web.client.URLBuilder;
import org.savara.gadget.web.client.auth.CurrentUser;
import org.savara.gadget.web.client.util.RestfulInvoker;
import org.savara.gadget.web.client.util.UUID;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jeff Yu
 * @date: 3/03/12
 */
public class TabLayout extends Composite {

    interface TabLayoutUiBinder extends UiBinder<Widget, TabLayout>{}

    private static TabLayoutUiBinder uiBinder = GWT.create(TabLayoutUiBinder.class);

    private String id;

    @UiField UnorderedList tabsBar;
    
    @UiField FlowPanel tabsContent;

    @UiField DivElement tabs;
    
    private ListItem addTabAnchorItem;
    
    private CurrentUser currentUser;
    
    private Map<String, String> tabNames = new HashMap<String, String>();
    
    private Map<String, String> indexIdMap = new HashMap<String, String>();
    
    private static int index = 0;

    public TabLayout(CurrentUser user) {
        currentUser = user;
        id = "tabs-" + UUID.uuid(4);
        initWidget(uiBinder.createAndBindUi(this));
        tabs.setId(id);
    }

    public void addTab(String pageId, String tabTitle, Widget widget){
        String tabContentId = getTabContentId(pageId);
        tabNames.put(tabContentId, tabTitle);

        addTabTitle(tabTitle, tabContentId);
        
        FlowPanel theContent = new FlowPanel();
        theContent.getElement().setId(tabContentId);
        theContent.add(widget);
        tabsContent.add(theContent);
        
        indexIdMap.put(String.valueOf(index), pageId);
        index ++;
    }

    public void setTabAnchor(Anchor anchor) {
        addTabAnchorItem = new ListItem();
        addTabAnchorItem.add(anchor);
        tabsBar.add(addTabAnchorItem);
    }

    public void addTabAnchor() {
        tabsBar.add(addTabAnchorItem);
    }

    private void addTabTitle(String tabTitle, String tabContentId) {
        ListItem li = new ListItem();
        li.getElement().setClassName("ui-state-default ui-corner-top");
        Anchor anchor = new Anchor();
        anchor.setHref("#" + tabContentId);
        anchor.setText(tabTitle);
        li.add(anchor);

        InlineLabel removeBtn = new InlineLabel();
        removeBtn.setText("remove");
        removeBtn.setStyleName("ui-icon ui-icon-close");
        li.add(removeBtn);
        
        tabsBar.add(li);

    }
    
    public void insertTab(String pageId, String tabTitle, Widget widget) {
        String tabContentId = getTabContentId(pageId);

        FlowPanel theContent = new FlowPanel();
        theContent.getElement().setId(tabContentId);
        theContent.add(widget);
        tabsContent.add(theContent);

        int theIndex = tabNames.size();

        tabNames.put(tabContentId, tabTitle);
        indexIdMap.put(String.valueOf(index), pageId);
        index = index + 1;

        tabsBar.remove(addTabAnchorItem);

        addNewTab(id, tabContentId, tabTitle, theIndex);

        tabsBar.add(addTabAnchorItem);
        
    }
    
    private String getTabContentId(String pageId) {
        return "tab-content-" + pageId;
    }

    public void onAttach() {
        super.onAttach();
    }

    public void initializeTab() {
        initTabs(this, id);
        registerCloseEvent(this,id);
    }

    public void clearAllTabs(){
        for(String contentId : tabNames.keySet()) {
            removeTab(id, contentId);
        }
        tabsBar.remove(addTabAnchorItem);
        destroyTab(id);
        index = 0;
    }
    
    private void setCurrentPage(Long indexId) {
        String theIndexId = String.valueOf(indexId);
        String pageId = indexIdMap.get(theIndexId);
        currentUser.setCurrentPage(Long.valueOf(pageId));
    }
    
    private void removePage(Long indexId) {
        String theIndexId = String.valueOf(indexId);
        String pageId = indexIdMap.get(theIndexId);
        RestfulInvoker.invoke(RequestBuilder.POST, URLBuilder.getRemovePageURL(Long.valueOf(pageId).longValue()),
                null, new RestfulInvoker.Response() {
                    public void onResponseReceived(Request request, Response response) {
                           //TODO:
                    }
        });
    }
    
    public void selectCurrentActiveTab() {
        String tabContentId = getTabContentId(String.valueOf(currentUser.getCurrentPage()));
        selectTab(id, tabContentId);
    }

    /**
     * JSNI methods
     */

    private static native void initTabs(final TabLayout layout, String id) /*-{
        $wnd.$('#'+id).tabs({
            tabTemplate: "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>remove</span></li>",
            select: function(event, ui) {
                layout.@org.savara.gadget.web.client.widgets.TabLayout::setCurrentPage(Ljava/lang/Long;)(ui.index);
            }
        });
    }-*/;

    private static native void selectTab(String id, String tabContentId) /*-{
        var theTabs = $wnd.$('#'+id).tabs();
        theTabs.tabs("select","#"+tabContentId);
    }-*/;

    private static native void addNewTab(String id, String tabContentId, String tabTitle, int index) /*-{
        var theTabs = $wnd.$('#'+id).tabs();
        $wnd.$('#'+id).tabs("add", "#"+tabContentId, tabTitle, index);
        theTabs.tabs("select","#"+tabContentId);
    }-*/;

    private static native void removeTab(String id, String tabContentId) /*-{
        var theTabs = $wnd.$('#'+id).tabs();
        theTabs.tabs("remove","#"+tabContentId);
    }-*/;

    private static native void destroyTab(String id) /*-{
        var theTabs = $wnd.$('#'+id).tabs();
        theTabs.tabs("destroy");
    }-*/;

    /**
     *  TODO: This is a hack, somehow couldn't attach the click event to removetBtn;
     * */
    private static native void registerCloseEvent(final TabLayout layout, String id) /*-{
        $wnd.$('#'+id + ' span.ui-icon-close').live('click', function(){
            var theTabs = $wnd.$('#'+id).tabs();
            var index = $wnd.$(this).parent().index();
            if (index > -1) {
                layout.@org.savara.gadget.web.client.widgets.TabLayout::removePage(Ljava/lang/Long;)(index);
                theTabs.tabs('remove', index);
            }
        });
    }-*/;


}

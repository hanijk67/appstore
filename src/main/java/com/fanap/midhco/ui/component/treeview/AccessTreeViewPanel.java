package com.fanap.midhco.ui.component.treeview;

import com.fanap.midhco.appstore.service.security.AccessService;
import com.fanap.midhco.ui.access.Access;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import java.util.*;

/**
 * Created by admin123 on 3/22/15.
 */
public class AccessTreeViewPanel extends FormComponentPanel {
    static final Logger logger = Logger.getLogger(AccessTreeViewPanel.class);

    private static ResourceReference jsTree = new JavaScriptResourceReference(
            AccessTreeViewPanel.class, "res/jstree.js");

    private static ResourceReference jsTree_search = new JavaScriptResourceReference(
            AccessTreeViewPanel.class, "res/jstree.search.js");

    private static CssResourceReference CSS = new CssResourceReference(AccessTreeViewPanel.class, "res/style.css");

    String mainMarkupId;

    Set<TreeNode> nodes = new HashSet<TreeNode>();
    Map<Integer, TreeNode> nodeMap = new HashMap<Integer, TreeNode>();

    String jsTreeBuildString;
    WebMarkupContainer treePanelDiv;
    AjaxEventBehavior changeBehaviour;

    HiddenField selected_idsField;
    boolean isAjaxy;
    byte[] deniedPerms;

    String searchBoxMarkupId;
    List<byte[]> additionalPerms;
    List<TreeNode> additionalNodes = null;

    public AccessTreeViewPanel(String id) {
        this(id, false, null);
    }

    public AccessTreeViewPanel(String id, boolean isAjaxy) {
        this(id, isAjaxy, null);
    }

    public AccessTreeViewPanel(String id, boolean isAjaxy, byte[] deniedPerms) {
        this(id, isAjaxy, deniedPerms, null);
    }

    public AccessTreeViewPanel(String id, boolean isAjaxy, byte[] deniedPerms, List<byte[]> additionalPerms) {
        super(id);


        this.isAjaxy = isAjaxy;
        this.deniedPerms = deniedPerms;
        this.additionalPerms = additionalPerms;

        treePanelDiv = new WebMarkupContainer("treePanelDiv");
        treePanelDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        add(treePanelDiv);

        selected_idsField = new HiddenField("selected_ids");
        selected_idsField.setModel(new Model());
        add(selected_idsField);

        TextField searchBox = new TextField("searchBox");
        searchBox.setModel(new Model());
        searchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        searchBoxMarkupId = searchBox.getMarkupId();
        add(searchBox);

        mainMarkupId = treePanelDiv.getMarkupId();

        changeBehaviour = new AjaxEventBehavior("select") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                setSelectedValue(target);

                Set<TreeNode> selectedNodes = new HashSet<TreeNode>();
                for (Integer id : nodeMap.keySet())
                    if (nodeMap.get(id).isSelected())
                        selectedNodes.add(nodeMap.get(id));
                byte[] bytes = getBytes();
                onChange(target, new HashSet<TreeNode>(selectedNodes), bytes);
            }
        };

        add(changeBehaviour);
    }

    protected void onChange(AjaxRequestTarget target, Set<TreeNode> selectedNodes, byte[] bytes) {
    }

    @Override
    public void renderHead(IHeaderResponse response) {

        response.render(JavaScriptHeaderItem.forReference(jsTree));
        response.render(JavaScriptHeaderItem.forReference(jsTree_search));
        response.render(CssHeaderItem.forReference(CSS));

        if (jsTreeBuildString != null) {
            response.render(OnDomReadyHeaderItem.forScript(jsTreeBuildString));
        }

        response.render(OnDomReadyHeaderItem.forScript("$('#" + searchBoxMarkupId + "').keydown(function(e) {\n" +
                "    if(e.which == 13) {\n" +
                "stopEventPropagation(e);" +
                "        $('#" + mainMarkupId + "').jstree(true).search(" +
                "$('#" + searchBoxMarkupId + "').val());" +
                "return false;" +
                "    }});"));

        super.renderHead(response);
    }

    String selected_ids;

    private void setSelectedValue(AjaxRequestTarget target) {
        try {
            IRequestParameters requestParameters =
                    RequestCycle.get().getRequest().getRequestParameters();
            StringValue select_ids_StringValue = requestParameters.getParameterValue("selected_ids");

            if (select_ids_StringValue != null) {
                selected_ids = selected_ids.toString();

                for (Integer s : nodeMap.keySet()) {
                    TreeNode treeNode = nodeMap.get(s);
                    treeNode.setSelected(false);
                }

                if (selected_ids != null && !selected_ids.trim().isEmpty()) {
                    String[] selectedIdsSplitted = selected_ids.split(",");
                    for (String s : selectedIdsSplitted) {
                        TreeNode treeNode = nodeMap.get(Integer.parseInt(s));
                        treeNode.setSelected(true);
                    }
                }
            } else {
                logger.debug("No selected_ids found on AccessTreeViewPanel component! .... ");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onBeforeRender() {
        Object objectModel = getModelObject();
        byte[] bytes = null;
        if (objectModel != null)
            bytes = (byte[]) objectModel;

        if (this.additionalPerms != null && !this.additionalPerms.isEmpty()) {
            additionalNodes = new ArrayList<TreeNode>();
            byte[] firstBytes = this.additionalPerms.get(0);

            for (int i = 1; i < this.additionalPerms.size(); i++) {
                byte[] addBytes = this.additionalPerms.get(i);
                firstBytes = AccessService.addPermissions(firstBytes, addBytes);
            }

            bytes = AccessService.addPermissions(bytes, firstBytes);
            additionalNodes.addAll(Access.getAccessesAsTreeNodes(firstBytes));
        }


        nodes = Access.getAccessesAsTreeNodes(bytes);

        Map<Integer, TreeNode> deniedMap = new HashMap<Integer, TreeNode>();
        if (deniedPerms != null) {
            Set<TreeNode> deniedNodes = Access.getAccessesAsTreeNodes(deniedPerms);
            if (deniedNodes != null && !deniedNodes.isEmpty()) {
                for (TreeNode treeNode : deniedNodes) {
                    if (treeNode.isSelected()) {
                        deniedMap.put(treeNode.getId().intValue(), treeNode);
                        TreeNode parent = treeNode.parent;
                        while (parent != null) {
                            deniedMap.put(parent.getId().intValue(), parent);
                            parent = parent.parent;
                        }
                    }
                }
            }
        }


        StringBuilder sb = new StringBuilder();
        buildJsTree(findRootNodes(nodes), sb, deniedMap);

        StringBuilder jsTreeStringBuilder = new StringBuilder("");
        jsTreeStringBuilder.append("$.jstree.defaults.search.show_only_matches = true;");
        jsTreeStringBuilder.append("$.jstree.defaults.core.expand_selected_onload = false;");
        jsTreeStringBuilder.append("var destroyIsLaunched = true;$('#")
                .append(mainMarkupId)
                .append("').jstree('destroy')")
                .append(".off('changed.jstree').on('changed.jstree', function (e, data) {\n" +
                        "if(destroyIsLaunched) return;" +
                        " var noChangeTrigger = $('#" + mainMarkupId + "')[0].noChangeTrigger;" +
                        "if(noChangeTrigger) return;" +
                        "    var i, j, r = [];\nconsole.log(data);" +
                        "if(data.selected.length == 0) {" +
                        "$('#" + treePanelDiv.getMarkupId() + "').siblings('input[type=\"hidden\"]').val('edited');" +
                        "} else {" +
                        "    for(i = 0, j = data.selected.length; i < j; i++) {\n" +
                        "      r.push(data.instance.get_node(data.selected[i]).id);\n" +
                        "    }\n" +
                        "   $('#" + treePanelDiv.getMarkupId() + "').siblings('input[type=\"hidden\"]').val(r.join(','));" +
                        "}" +
                        (isAjaxy ?
                                " wicketAjaxPost('" + changeBehaviour.getCallbackUrl() + "', 'selected_ids=' + r.join(','));\n"
                                : "") +
                        "  })")
                .append(
                        ".on('ready.jstree', function() {destroyIsLaunched = false;})"
                )
                .append(".jstree({\n" +
                        "'plugins': [\"wholerow\", \"checkbox\", \"search\"],\n" +
                        "'core': {\n" +
                        "'data': [");
        jsTreeStringBuilder.append(sb);
        jsTreeStringBuilder.append("],\n" +
                "            'themes': {\n" +
                "            }\n" +
                "        }\n" +
                "    });\n");
        jsTreeBuildString = jsTreeStringBuilder.toString();

        super.onBeforeRender();
    }

    public void updateSelectedNodes(AjaxRequestTarget target, byte[] bytes) {
        if (bytes == null) {
            bytes = (byte[]) getModelObject();
            if (bytes == null)
                for (TreeNode node : nodes)
                    node.setSelected(false);
        }
        if (bytes != null)
            nodes = Access.getAccessesAsTreeNodes(bytes);

        Map<Integer, TreeNode> deniedMap = new HashMap<Integer, TreeNode>();
        if (deniedPerms != null) {
            Set<TreeNode> deniedNodes = Access.getAccessesAsTreeNodes(deniedPerms);
            if (deniedNodes != null && !deniedNodes.isEmpty()) {
                for (TreeNode treeNode : deniedNodes) {
                    if (treeNode.isSelected()) {
                        deniedMap.put(treeNode.getId().intValue(), treeNode);
                        TreeNode parent = treeNode.parent;
                        while (parent != null) {
                            deniedMap.put(parent.getId().intValue(), parent);
                            parent = parent.parent;
                        }
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("$('#" + mainMarkupId + "')[0].noChangeTrigger = true;");
        for (TreeNode node : nodes) {
            if (node.isSelected() && deniedMap.get(node.getId().intValue()) != null) {
                sb.append("$('#" + mainMarkupId + "').jstree().uncheck_node(" + node.getId() + ");");
            } else if (node.isSelected())
                sb.append("$('#" + mainMarkupId + "').jstree().check_node(" + node.getId() + ");");
        }
        sb.append("$('#" + mainMarkupId + "')[0].noChangeTrigger = false;");
        String f = sb.toString();
        target.appendJavaScript(f);
    }

    @Override
    public void convertInput() {
        byte[] bytes = getBytes();
        setConvertedInput(bytes);
    }

    private byte[] getBytes() {
        String selected_ids = (String) selected_idsField.getConvertedInput();

        if (selected_ids != null && !selected_ids.isEmpty()) {

            for (Integer s : nodeMap.keySet()) {
                TreeNode treeNode = nodeMap.get(s);
                treeNode.setSelected(false);
            }
            if (selected_ids != null && !selected_ids.trim().isEmpty()) {
                if (!selected_ids.equals("edited")) {

                    String[] selectedIdsSplitted = selected_ids.split(",");
                    for (String s : selectedIdsSplitted) {
                        TreeNode treeNode = nodeMap.get(Integer.parseInt(s));
                        treeNode.setSelected(true);
                    }
                }
            }
        }


        SortedSet<TreeNode> selectedNodes = new TreeSet<TreeNode>();
        for (Integer s : nodeMap.keySet()) {
            TreeNode tempNode = nodeMap.get(s);
            if (tempNode.isSelected()) {
                logger.debug("------------->" + tempNode.getId());
                if (additionalNodes != null) {
                    int ix = additionalNodes.indexOf(tempNode);
                    if (ix != -1) {
                        TreeNode addNode = additionalNodes.get(ix);
                        if (addNode.isSelected())
                            continue;
                    }
                }
                selectedNodes.add(nodeMap.get(s));
            }
        }
        byte[] bytes = null;
        if (!selectedNodes.isEmpty()) {
            int lastValue = selectedNodes.last().getId().intValue();
            lastValue++;
            bytes = new byte[lastValue / 8 + (lastValue % 8 > 0 ? 1 : 0)];
            for (TreeNode treeNode : selectedNodes) {
                int bitNo = treeNode.getId().intValue();
                bytes[bitNo / 8] = (byte) (bytes[bitNo / 8] | (byte) Math.pow(2, bitNo % 8));
            }
        }
        return bytes;
    }

    private Set<TreeNode> findRootNodes(Set<TreeNode> nodes) {
        Set<TreeNode> retSet = new HashSet<TreeNode>();
        for (TreeNode treeNode : nodes) {
            if (treeNode.getParent() == null) {
                retSet.add(treeNode);
            }
        }
        return retSet;
    }

    private void buildJsTree(Set<TreeNode> nodes, StringBuilder jsTreeStringBuilder, Map<Integer, TreeNode> deniedMap) {
        int nodesSize = nodes.size();
        int counter = 0;
        for (TreeNode treeNode : nodes) {
            counter++;
            nodeMap.put(treeNode.getId().intValue(), treeNode);
            jsTreeStringBuilder.append("{")
                    .append("\"text\":").append("\"").append(treeNode.getTitle()).append("\"");
            jsTreeStringBuilder.append(", \"id\":").append("\"").append(treeNode.getId()).append("\"");

            Set<TreeNode> children = treeNode.getChildren();
            if (children != null && !children.isEmpty()) {
                jsTreeStringBuilder.append(", \"children\":[");
                buildJsTree(children, jsTreeStringBuilder, deniedMap);
                jsTreeStringBuilder.append("]");
            }


            if (treeNode.isSelected() && deniedMap.get(treeNode.getId().intValue()) == null) {
                jsTreeStringBuilder.append(",")
                        .append("\"state\": {\n" +
                                "\"selected\": true\n" +
                                "}");
            } else if (deniedMap.get(treeNode.getId().intValue()) != null) {
                jsTreeStringBuilder.append(",")
                        .append("\"state\": {\n" +
                                "\"selected\": false\n" +
                                "}");
                treeNode.setSelected(false);
            }

            jsTreeStringBuilder.append("}");
            if (counter < nodesSize)
                jsTreeStringBuilder.append(",");
        }
    }

    public void setDeniedPerms(byte[] deniedPerms) {
        this.deniedPerms = deniedPerms;
    }
}

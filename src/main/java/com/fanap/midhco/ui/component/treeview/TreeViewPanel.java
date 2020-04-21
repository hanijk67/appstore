package com.fanap.midhco.ui.component.treeview;

import com.fanap.midhco.appstore.entities.AppPackage;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.component.IParentListenable;
import com.fanap.midhco.ui.component.ISelectable;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
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
 * Created by admin123 on 8/17/2015.
 */
public class TreeViewPanel<G> extends FormComponentPanel implements ISelectable, IParentListenable {
    static final Logger logger = Logger.getLogger(TreeViewPanel.class);

    private static ResourceReference jsTree = new JavaScriptResourceReference(
            TreeViewPanel.class, "res/jstree.js");

    private static ResourceReference jsTree_search = new JavaScriptResourceReference(
            TreeViewPanel.class, "res/jstree.search.js");

    private static CssResourceReference CSS = new CssResourceReference(TreeViewPanel.class, "res/style.css");

    String mainMarkupId;

    Set<TreeNode<G>> nodes = new HashSet<TreeNode<G>>();
    Map<Integer, TreeNode<G>> nodeMap = new HashMap<Integer, TreeNode<G>>();

    String jsTreeBuildString;
    WebMarkupContainer treePanelDiv;
    AjaxEventBehavior changeBehaviour;
    boolean partialLoading = false;

    HiddenField selected_idsField;
    boolean isMultiple = false;
    String searchBoxMarkupId;

    IParentListner parentListener;

    boolean isAjaxy;

    Form form;

    public TreeViewPanel(String id, boolean isMultiple, ITreeNodeProvider<G> provider) {
        this(id, isMultiple, false, provider);
    }

    public TreeViewPanel(String id, boolean isMultiple, boolean isAjaxy,ITreeNodeProvider<G> provider) {
        super(id);

        this.isAjaxy = isAjaxy;

        form = new Form("form");
        add(form);

        this.isMultiple = isMultiple;

        treePanelDiv = new WebMarkupContainer("treePanelDiv");
        treePanelDiv.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        form.add(treePanelDiv);

        selected_idsField = new HiddenField("selected_ids");
        selected_idsField.setModel(new Model());
        form.add(selected_idsField);

        mainMarkupId = treePanelDiv.getMarkupId();

        changeBehaviour = new AjaxEventBehavior("select") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                setSelectedValue(target);
            }
        };

        add(changeBehaviour);

        if (!partialLoading)
            this.nodes = provider.getNodes(null);

        TextField searchBox = new TextField("searchBox");
        searchBox.setModel(new Model());
        searchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
        searchBoxMarkupId = searchBox.getMarkupId();
        form.add(searchBox);

        form.add(new AjaxButton("select", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (parentListener != null)
                    parentListener.onChildFinished(target, null, TreeViewPanel.this);
            }
        });
    }

    private void setSelectedValue(AjaxRequestTarget target) {
        try {
            IRequestParameters requestParameters =
                    RequestCycle.get().getRequest().getRequestParameters();
            StringValue select_ids_StringValue = requestParameters.getParameterValue("selected_ids");

            if(select_ids_StringValue != null) {
                String selected_ids = select_ids_StringValue.toString();
                Collection<G> selectedNodes = getSelectedNodes(selected_ids);
                onUpdate(target, selectedNodes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onUpdate(AjaxRequestTarget target, Collection<G> selectedNodes) {
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

    @Override
    protected void onBeforeRender() {
        Object objectModel = getModelObject();
        byte[] bytes = null;

        StringBuilder sb = new StringBuilder();
        buildJsTree(findRootNodes(nodes), sb);

        if (objectModel != null) {
            if (isMultiple) {
                Set<G> nodesSent = (Set<G>) objectModel;
                for (G node : nodesSent) {
                    for (TreeNode<G> existNode : nodeMap.values()) {
                        if (existNode.getSelf().equals(node)) {
                            existNode.setSelected(true);
                            break;
                        }
                    }
                }
            } else {
                G nodeSent = (G) objectModel;
                for (TreeNode<G> existNode : nodeMap.values()) {
                    if (existNode.getSelf().equals(nodeSent)) {
                        existNode.setSelected(true);
                        break;
                    }
                }
            }

            sb = new StringBuilder();
            buildJsTree(findRootNodes(nodes), sb);
        }

        StringBuilder jsTreeStringBuilder = new StringBuilder("");
        jsTreeStringBuilder.append("$.jstree.defaults.search.show_only_matches = true;");
        jsTreeStringBuilder.append("$.jstree.defaults.core.expand_selected_onload = false;");
        jsTreeStringBuilder.append("$('#")
                .append(mainMarkupId)
                .append("')")
                .append(".on('changed.jstree', function (e, data) {\n" +
                        "    var i, j, r = [];\n" +
                        "    for(i = 0, j = data.selected.length; i < j; i++) {\n" +
                        "      r.push(data.instance.get_node(data.selected[i]).id);\n" +
                        "    }\n" +
                        " $('#" + treePanelDiv.getMarkupId() + "').siblings('input[type=\"hidden\"]').val(r.join(','));" +
                        (isAjaxy ?

                                "Wicket.Ajax.post({'u':'" + changeBehaviour.getCallbackUrl() + "&selected_ids=' + r.join(',')});" : "")+
                        "  })")
                .append(isMultiple ?
                        ".on('ready.jstree', function() {$(\"#" + mainMarkupId + "\").jstree(\"close_all\");})" : "")
                .append(".jstree({\n");

        if (isMultiple)
            jsTreeStringBuilder.append(
                    "'plugins': [\"wholerow\", \"checkbox\", \"search\"],\n");
        else
            jsTreeStringBuilder.append(
                    "'plugins': [\"search\"],\n");

        jsTreeStringBuilder.append("'core': {\n" +
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

    @Override
    public void convertInput() {
        String selected_ids = (String) selected_idsField.getConvertedInput();
        Set selectedNodes = getSelectedNodes(selected_ids);

        if (selectedNodes.isEmpty())
            setConvertedInput(null);
        else {
            if (!isMultiple)
                setConvertedInput(new ArrayList(selectedNodes).get(0));
            else
                setConvertedInput(selectedNodes);
        }
    }

    private Set<G> getSelectedNodes(String selected_ids) {
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

        Set<G> selectedNodes = new HashSet<G>();
        for (Integer s : nodeMap.keySet())
            if (nodeMap.get(s).isSelected()) {
                selectedNodes.add(nodeMap.get(s).getSelf());
            }

        return selectedNodes;
    }

    private Set<TreeNode> findRootNodes(Set<TreeNode<G>> nodes) {
        Set<TreeNode> retSet = new HashSet<TreeNode>();
        for (TreeNode treeNode : nodes) {
            if (treeNode.getParent() == null) {
                retSet.add(treeNode);
            }
        }
        return retSet;
    }

    private void buildJsTree(Set<TreeNode> nodes, StringBuilder jsTreeStringBuilder) {
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
                buildJsTree(children, jsTreeStringBuilder);
                jsTreeStringBuilder.append("]");
            }

            if (treeNode.isSelected()) {
                jsTreeStringBuilder.append(",")
                        .append("\"state\": {\n" +
                                "\"selected\": true\n" +
                                "}");
            }

            jsTreeStringBuilder.append("}");
            if (counter < nodesSize)
                jsTreeStringBuilder.append(",");
        }
    }

    public Collection<Object> getSelection() {
        String selected_ids = (String) selected_idsField.getConvertedInput();
        return (Collection<Object>) getSelectedNodes(selected_ids);
    }

    @Override
    public void setParentListener(IParentListner parentListener) {
        this.parentListener = parentListener;
    }
}

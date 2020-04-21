package com.fanap.midhco.ui.pages.app;

import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.comment.CommentService;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.IParentListner;
import com.fanap.midhco.ui.access.Access;
import com.fanap.midhco.ui.access.Authorize;
import com.fanap.midhco.ui.access.PrincipalUtil;
import com.fanap.midhco.ui.component.SelectionMode;
import com.fanap.midhco.ui.component.modal.BootStrapModal;
import com.fanap.midhco.ui.component.switchbox.SwitchBox;
import com.fanap.midhco.ui.component.table.MyAjaxDataTable;
import com.fanap.midhco.ui.component.table.column.IndexColumn;
import com.fanap.midhco.ui.component.table.column.MyAbstractColumnWithDataTag;
import com.fanap.midhco.ui.component.table.column.PersianDateColumn;
import com.fanap.midhco.ui.component.textareapanel.TextAreaPanel;
import io.searchbox.client.JestResult;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.ComponentDetachableModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import java.io.Serializable;
import java.util.*;

/**
 * Created by A.Moshiri on 9/24/2017.
 */

@Authorize(view = Access.COMMENT_LIST)
public class AppCommentForm extends BasePanel {
    BootStrapModal modal = new BootStrapModal("modal");
    AppCommentListSortableDataProvider dp = new AppCommentListSortableDataProvider();
    List<CommentService.ElasticCommentVO> selectedCommentModels = new ArrayList<>();

    Form form;
    FeedbackPanel feedbackPabel;
    MyAjaxDataTable table;
    Map<String, String> descriptionMap = new HashMap<>();
    SwitchBox isApprovedSwitchBox;

    public AppCommentForm() {
        this(MAIN_PANEL_ID, new CommentService.CommentVOCriteria(), SelectionMode.None);
    }

    public AppCommentForm(String id, final CommentService.CommentVOCriteria criteria, final SelectionMode selectionMode) {
        super(id);
        try {
            add(modal);

            feedbackPabel = new FeedbackPanel("feedbackPabel");
            feedbackPabel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            add(feedbackPabel);

            form = new Form("form", new ComponentDetachableModel());

            isApprovedSwitchBox = new SwitchBox("isApproved", getString("ApprovalState.approved"), getString("ApprovalState.disApproved"), true) {
                @Override
                protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                    try {
                        criteria.approved = currentState;
                        dp.setCriteria(criteria);
                        table.setVisible(true);
//                        if (selectionMode.isSelectable())
//                            target.add(AppCommentForm.this.get("select").setVisible(true));
                        target.add(table);
                    } catch (Exception ex) {
                        processException(target, ex);
                    }
                }
            };
            isApprovedSwitchBox.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            isApprovedSwitchBox.setLabel(new ResourceModel("app.comment.approve.status"));
            isApprovedSwitchBox.setModel(new Model<>());
            isApprovedSwitchBox.setRequired(true);
            form.add(isApprovedSwitchBox);


            form.add(new AjaxLink("cancel") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    modal.close(target);
                }
            });

            add(form);

            dp.setCriteria(criteria);
            table = new MyAjaxDataTable("table", getColumns(), dp, 10);
            table.setSelectionMode(selectionMode);
            table.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
            add(table);

        } catch (Exception ex) {
            throw new AppStoreRuntimeException(ex);
        } finally {
        }
    }

    private List<IColumn> getColumns() {
        List<IColumn> columnList = new ArrayList<IColumn>();
        columnList.add(new IndexColumn());

/*
        columnList.add(new AbstractColumn(new ResourceModel("label.select")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {

//                AjaxCheckBox selectCheckBox = new AjaxCheckBox(componentId, rowModel) {
                AjaxCheckBox selectCheckBox = new AjaxCheckBox(componentId , new PropertyModel(rowModel, "selected")) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        boolean isSelected = getConvertedInput();
                        CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();

                        if (isSelected) {
                            selectedCommentModels.add(elasticCommentVO);
                        } else {
                            if (selectedCommentModels.contains(elasticCommentVO)) {
                                selectedCommentModels.remove(elasticCommentVO);
                            }
                        }
                    }
                };
                cellItem.add(selectCheckBox);
            }
        });
*/


//for without myAbstractcolumnWithTag
/*


        columnList.add(new AbstractColumn(new ResourceModel("app.comment.text")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
                TextAreaPanel textAreaPanel = new TextAreaPanel(componentId, elasticCommentVO.getCommentText(), true);
                textAreaPanel.add(new AttributeAppender("rtl", true));

                textAreaPanel.setModel(new Model<>(elasticCommentVO.getCommentText()));
                cellItem.add(textAreaPanel);
            }
        });

        columnList.add(new PropertyColumn(new ResourceModel("label.creatorUser"), "userName", "userName"));

        columnList.add(new AbstractColumn(new ResourceModel("app.comment.approve.status")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                IModel inputRowModel = rowModel;
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.COMMENT_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("ApprovalState.approved").getObject(), new ResourceModel("ApprovalState.disApproved").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            try {
                                elasticCommentVO.setApproved(currentState);
                                if (currentState) {
                                    elasticCommentVO.setApprovalDate(new Date(DateTime.now().getDateTimeLong()));
                                }
                                CommentService.ElasticApproveVO elasticApproveVO = CommentService.Instance.buildApproveVoByElasticCommentVo(elasticCommentVO);
                                elasticApproveVO.setDescription(descriptionMap.get(elasticCommentVO.getId()));
                                JestResult jestResultApprove = CommentService.Instance.approveComment(elasticCommentVO.getId(), elasticApproveVO);
                                JestResult jestResult = CommentService.Instance.insertCommentForAppMainPackage(elasticCommentVO);

                            } catch (Exception ex) {
                                processException(target, ex);
                            }
                        }
                    };
                    switchBox.setModel(new Model<>(elasticCommentVO.getApproved()));
                    cellItem.add(switchBox);
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });

        if (!PrincipalUtil.hasPermission(Access.COMMENT_EDIT))
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item item, String s, IModel iModel) {

                }
            });

        columnList.add(new AbstractColumn(new ResourceModel("app.comment.description")) {
            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.COMMENT_EDIT)) {
                    TextAreaPanel textAreaPanel = new TextAreaPanel(componentId,"", false, true) {
                        @Override
                        public void onUpdate(AjaxRequestTarget target) {
                            String descriptionString = (String) getConvertedInput();
                            descriptionMap.put(elasticCommentVO.getId(), descriptionString);
                        }

                        @Override
                        protected void onModelChanged() {
                            System.out.println("salam ali");
                        }
                    };
                    textAreaPanel.setModel(new Model<>(""));
                    textAreaPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
                    cellItem.add(textAreaPanel);
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });

        if (!PrincipalUtil.hasPermission(Access.COMMENT_EDIT))
            columnList.add(new AbstractColumn(new Model<String>("")) {
                @Override
                public void populateItem(Item item, String s, IModel iModel) {

                }
            });

*/


//for myABstractColumnWithTag

        columnList.add(new PropertyColumn(new ResourceModel("label.id"), "id", "id"));

        columnList.add(new PersianDateColumn(new ResourceModel("app.comment.date"), "lastModifyDate", "lastModifyDate"));

        columnList.add(new MyAbstractColumnWithDataTag(new ResourceModel("app.comment.text")) {

            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
                TextAreaPanel textAreaPanel = new TextAreaPanel(componentId, elasticCommentVO.getCommentText(), true);
                textAreaPanel.add(new AttributeAppender("rtl", true));

                textAreaPanel.setModel(new Model<>(elasticCommentVO.getCommentText()));
                cellItem.add(textAreaPanel);
            }

            @Override
            public Object getDataTag(Object o) {
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) o;
                return elasticCommentVO.getCommentText();

            }
        });


        columnList.add(new PropertyColumn(new ResourceModel("label.creatorUser"), "userName", "userName"));

//        columnList.add(new AbstractColumn(new ResourceModel("app.comment.description")) {
//
//            @Override
//            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
//                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
//                if (PrincipalUtil.hasPermission(Access.COMMENT_EDIT)) {
//                    TextAreaPanel textAreaPanel = new TextAreaPanel(componentId, "", false, true) {
//                        @Override
//                        public void onUpdate(AjaxRequestTarget target) {
//                            String descriptionString = (String) getConvertedInput();
//                            descriptionMap.put(elasticCommentVO.getId(), descriptionString);
//                        }
//
//                    };
//                    textAreaPanel.setModel(new Model<>(""));
//                    textAreaPanel.setOutputMarkupId(true).setOutputMarkupPlaceholderTag(true);
//                    cellItem.add(textAreaPanel);
//                } else {
//                    cellItem.add(new Label(componentId, new Model<>("")));
//                }
//            }
//        });


        columnList.add(new MyAbstractColumnWithDataTag(new ResourceModel("app.comment.approve.status")) {
            @Override
            public Object getDataTag(Object o) {
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) o;

                return (elasticCommentVO.getApproved() != null && elasticCommentVO.getApproved()) ? getString("ApprovalState.approved") : getString("ApprovalState.disApproved");
            }

            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                IModel inputRowModel = rowModel;
                CommentService.ElasticCommentVO elasticCommentVO = (CommentService.ElasticCommentVO) rowModel.getObject();
                if (PrincipalUtil.hasPermission(Access.COMMENT_EDIT)) {
                    SwitchBox switchBox = new SwitchBox(componentId, new ResourceModel("ApprovalState.approved").getObject(), new ResourceModel("ApprovalState.disApproved").getObject(), true) {
                        @Override
                        protected void onChange(AjaxRequestTarget target, Boolean currentState) {
                            try {
                                elasticCommentVO.setApproved(currentState);
                                if (currentState) {
                                    elasticCommentVO.setApprovalDate(new Date(DateTime.now().getDateTimeLong()));
                                }
                                CommentService.ElasticApproveVO elasticApproveVO = CommentService.Instance.buildApproveVoByElasticCommentVo(elasticCommentVO);
                                elasticApproveVO.setDescription(descriptionMap.get(elasticCommentVO.getId()));
                                JestResult jestResultApprove = CommentService.Instance.approveComment(elasticCommentVO.getId(), elasticApproveVO);
                                JestResult jestResult = CommentService.Instance.insertCommentForAppMainPackage(elasticCommentVO);

                            } catch (Exception ex) {
                                processException(target, ex);
                            }
                        }
                    };
                    switchBox.setModel(new Model<>(elasticCommentVO.getApproved()));
                    cellItem.add(switchBox);
                } else {
                    cellItem.add(new Label(componentId, new Model<>("")));
                }
            }
        });

        return columnList;
    }

    public static class AppCommentListSortableDataProvider extends SortableDataProvider {
        public CommentService.CommentVOCriteria commentVOCriteria;
        List<CommentService.ElasticCommentVO> commentVOS = null;

        public AppCommentListSortableDataProvider() {
            setSort("id", SortOrder.ASCENDING);
        }

        public void setCriteria(CommentService.CommentVOCriteria criteria) {
            this.commentVOCriteria = criteria;
        }

        @Override
        public Iterator iterator(long first, long count) {
            try {
                return commentVOS.iterator();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public long size() {
            try {
//                List<CommentService.ElasticCommentVO> elasticCommentVOList = CommentService.Instance.getCommentsForApp(commentVOCriteria, 0, -1, "approveDate", false);
                commentVOS = CommentService.Instance.getCommentsForApp(commentVOCriteria, 0, -1, "approveDate", false);
                for (CommentService.ElasticCommentVO elasticCommentVO : commentVOS){
                    if (elasticCommentVO.getLastModifyDate()!=null) {
                        elasticCommentVO.setLastModifyDate(new Date(elasticCommentVO.getLastModifyDate()).getTime());
                    }
                }
                List<String> parentCommentIdList = new ArrayList<>();

//                for (CommentService.ElasticCommentVO elasticCommentVO : elasticCommentVOList) {
//                    if (elasticCommentVO.getId() != null) {
//                        parentCommentIdList.add(elasticCommentVO.getId());
//                        commentVOS =  CommentService.Instance.getApprovalCommentList( elasticCommentVO.getId() ,commentVOCriteria.approved== null ? false : commentVOCriteria.approved  );
//                        commentVOS.add()
//                    }
//                }
//                commentVOS = CommentService.Instance.getCommentsForApp(commentVOCriteria, 0, -1, "lastModifyDate", false);
                if (commentVOS != null) {
                    return commentVOS.size();
                }
            } catch (Exception e) {
            }
            return 0;
        }

        @Override
        public IModel model(Object object) {
            return new Model((Serializable) object);
        }
    }


}

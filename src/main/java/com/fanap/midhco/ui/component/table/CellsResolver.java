package com.fanap.midhco.ui.component.table;

import com.fanap.midhco.ui.component.HasLabel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.parser.filter.WicketTagIdentifier;
import org.apache.wicket.markup.resolver.IComponentResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Response;

import java.io.Serializable;

public class CellsResolver
        implements IComponentResolver {

    public CellsResolver() {
    }

    public Component resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag openTag) {
        Component intendedComponent = container.get(openTag.getId());

        if ((openTag instanceof WicketTag) && openTag.getName().equals("cells")) {
            WicketTag cellsTag = (WicketTag) openTag;
            int maxCol = cellsTag.getAttributes().getInt("maxCols");
            String headerCellClass = cellsTag.getAttributes().getString("tdClass", null);
            String cellClass = cellsTag.getAttributes().getString("thClass", null);
            String requiredClass = cellsTag.getAttributes().getString("requiredClass", null);
            markupStream.next();
            Response res = container.getResponse();
            int colCount = 0;

            while (markupStream.hasMore() && !markupStream.get().closes(openTag)) {
                MarkupElement element = markupStream.get();
                if ((element instanceof ComponentTag) && !markupStream.atCloseTag()) {
                    ComponentTag tag = (ComponentTag) element;
                    Component component = container.get(tag.getId());
                    if (component != null) {
                        if ((component instanceof FormComponent) || (component instanceof HasLabel)) {
                            boolean req = false;
                            IModel label;
                            if (component instanceof FormComponent) {
                                FormComponent fc = (FormComponent) component;
                                req = fc.isRequired();
                                label = fc.getLabel();
                            } else {
                                HasLabel hl = (HasLabel) component;
                                label = hl.getLabel();
                            }
                            if (component.isVisible()) {
                                if (colCount % maxCol == 0) {
                                    if (colCount > 0)
                                        res.write("</tr>");
                                    res.write("<tr>");
                                }
                                String required = "";
                                if (requiredClass != null) {
                                    Serializable mdk = component.getMetaData(MyMetaDataKeys.SEMI_REQUIRED);
                                    if (mdk != null && ((Boolean) mdk).booleanValue())
                                        required = "<span style='color:green'>*</span>";
                                    else
                                        required = req ? String.format("<span class=\"%s\">*</span>", new Object[]{
                                                requiredClass
                                        }) : "";
                                }
                                String hcc = headerCellClass == null ? "" : String.format("class=\"%s\"", new Object[]{
                                        headerCellClass
                                });
                                res.write(String.format("<th %s>%s%s</th>", new Object[]{
                                        hcc, label == null ? component.getId() : label.getObject(), required
                                }));
                                String cellClassSnippet = cellClass == null ? "" : String.format("class=\"%s\"", new Object[]{
                                        cellClass
                                });
                                Integer colSpan = (Integer) component.getMetaData(MyMetaDataKeys.COL_SPAN);
                                String colSpanSnippet = colSpan == null ? "" : String.format("colspan='%d'", new Object[]{
                                        colSpan
                                });
                                String style = (String) component.getMetaData(MyMetaDataKeys.STYLE);
                                String styleSnippet = style == null ? "" : String.format("style='%s'", new Object[]{
                                        style
                                });
                                res.write(String.format("<td %s %s %s>", new Object[]{
                                        cellClassSnippet, colSpanSnippet, styleSnippet
                                }));
                                component.render();
                                res.write("</td>");
                                colCount += colSpan == null ? 1 : colSpan.intValue() + 1;
                            } else {
                                markupStream.next();
                            }
                        } else {
                            markupStream.throwMarkupException(String.format("Component id=[%s] is not a FormComponent or HasLabel!", new Object[]{
                                    tag.getId()
                            }));
                        }
                    } else {
                        markupStream.throwMarkupException(String.format("Tag [%s] not supported in FormTable!", new Object[]{
                                tag.getName()
                        }));
                    }
                } else {
                    markupStream.next();
                }
            }
            int modulo = colCount % maxCol;
            if (modulo > 0) {
                for (int i = 0; i < maxCol - modulo; i++)
                    res.write("<td> </td><td> </td>");

            }
            res.write("</tr>");

        }

        return intendedComponent;
    }

    static {
        WicketTagIdentifier.registerWellKnownTagName("cells");
    }
}
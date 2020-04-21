package com.fanap.midhco.ui.component.imagePanel;

import com.fanap.midhco.appstore.applicationUtils.AppUtils;
import com.fanap.midhco.ui.BasePanel;
import com.fanap.midhco.ui.access.Anonymous;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.DynamicImageResource;

@Anonymous
public class ImagePanel extends BasePanel {
	private Image image;

	public ImagePanel(String id, String url, IModel title) {
		super(id);
		DynamicImageResource imageResource = new DynamicImageResource() {
			@Override
			protected byte[] getImageData(Attributes attributes) {
				return AppUtils.getImageAsBytes(url);
			}
		};
		image = new Image("image", imageResource);
		image.add(new AttributeAppender("title", title, " "));
		add(image);
	}

	public ImagePanel(String id, String url) {
		this(id, url, new Model());
	}
}

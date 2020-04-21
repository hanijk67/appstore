package test;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by admin123 on 7/10/2016.
 */
public class TestServlet extends HttpServlet {
    static final File repository = new File("c:\\tmp");
    private static DiskFileItemFactory factory = new DiskFileItemFactory(5000000, repository);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ServletFileUpload servletFileUpload = new ServletFileUpload(factory);
            servletFileUpload.setSizeMax(5000000);
            List<FileItem> fileItems = servletFileUpload.parseRequest(req);
            String key = req.getSession().getId() + (Math.random() * System.nanoTime());


            try {
                for (FileItem item : fileItems) {
                    System.out.println("extension is ... " + FilenameUtils.getExtension(item.getName()));

                    if (!item.isFormField()) {
                        File file = new File(item.getName());
                        String name = file.getName();
                        System.out.println("name: " + name);
                        System.out.println("length : " + file.length());

                    }
                }
            } catch (Exception ex) {
                key = "Error in file uploads :)";
            }

            resp.setCharacterEncoding("utf-8");
            resp.getWriter().write("SALAMSLSKLSML544645thfghrhKMNDL");
        } catch (Exception e) {
            throw new IOException("cannot upload file", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        doGet(request, resp);
    }
}

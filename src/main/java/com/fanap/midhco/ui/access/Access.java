package com.fanap.midhco.ui.access;

import com.fanap.midhco.appstore.applicationUtils.ConfigUtil;
import com.fanap.midhco.appstore.service.myException.AppStoreRuntimeException;
import com.fanap.midhco.ui.component.treeview.TreeNode;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by admin123 on 6/15/2016.
 */
public enum Access {
    NULL( -1, null, ""),

    ROOT(0, null, "ريشه"),

    BASIC_DATA(1, ROOT, "اطلاعات پايه"),

    USER_MANAGEMENT(2, BASIC_DATA, "مديريت کاربران"),

    USER(3, USER_MANAGEMENT, "کاربر"),
    USER_LIST(4, USER, "ليست کاربر"),
    USER_CREATE(5, USER, "ايجاد کاربر"),
    USER_EDIT(6, USER, "ويرايش کاربر"),

    ROLE(9, USER_MANAGEMENT, "نقش"),
    ROLE_LIST(10, ROLE, "ليست نقش"),
    ROLE_CREATE(11, ROLE, "ايجاد نقش"),
    ROLE_EDIT(12, ROLE, "ويرايش نقش"),
    ROLE_LIST_MSG_USERS_EDIT(14, ROLE, "ویرایش کاربران مجاز سیستم پیام رسانی"),

    CHANGE_PASSWORD(15, USER_MANAGEMENT, "تغيير کلمه عبور"),

    OS_MANAGEMENT(24, BASIC_DATA, "مدیریت پارسر"),
    OS(16, OS_MANAGEMENT, "پارسر"),
    OS_LIST(17, OS, "لیست پارسر"),
    OS_ADD(18, OS, "افزودن پارسر"),
    OS_EDIT(19, OS, "ویرایش پارسر"),

    OSTYPE(20, OS_MANAGEMENT, "نوع پارسر"),
    OSTYPE_LIST(21, OSTYPE, "لیست نوع پارسر"),
    OSTYPE_ADD(22, OSTYPE, "افزودن نوع پارسر"),
    OSTYPE_EDIT(23, OSTYPE, "ویرایش نوع پارسر"),

    APP_MANAGEMENT(25, ROOT, "مدیریت برنامه ها"),
    APP_LIST(26, APP_MANAGEMENT, "لیست برنامه ها"),
    APP_ADD(27, APP_MANAGEMENT, "افزودن برنامه"),
    APP_EDIT(28, APP_MANAGEMENT, "ویرایش برنامه"),
    APP_PUBLISH_UNPUBLISH(29, APP_MANAGEMENT, "انتشار برنامه"),
    APP_ADD_PACKAGE(31, APP_MANAGEMENT, "افزودن بسته"),
    APP_REMOVE_PACKAGE(32, APP_MANAGEMENT, "حذف بسته"),

    APPCATEGORY(33, BASIC_DATA, "دسته بندی برنامه"),
    APPCATEGORY_LIST(34, APPCATEGORY, "لیست دسته بندی برنامه"),
    APPCATEGORY_ADD(35, APPCATEGORY, "افزودن دسته بندی برنامه"),
    APPCATEGORY_EDIT(36, APPCATEGORY, "ویرایش دسته بندی برنامه"),

    DEVICE(37, BASIC_DATA, "دستگاه"),
    DEVICE_LIST(38, DEVICE, "لیست دستگاه"),
    DEVICE_ADD(39, DEVICE, "افزودن دستگاه"),
    DEVICE_EDIT(40, DEVICE, "ویرایش دستگاه"),


    TEST(41, ROOT, "تست"),
    TEST_LIST(42, TEST, "لیست تست"),
    TEST_ADD(43, TEST, "افزودن تست"),
    TEST_EDIT(44, TEST, "ویرایش تست"),


    HANDLERAPP(45, OS, "برنامه اداره کننده"),
    HANDLERAPP_LIST(46, HANDLERAPP, "لیست برنامه اداره کننده"),
    HANDLERAPP_ADD(47, HANDLERAPP, "افزودن برنامه اداره کننده"),
    HANDLERAPP_EDIT(48, HANDLERAPP, "ویرایش برنامه اداره کننده"),

    ANOUNCEMENT(49, ROOT, "اعلان"),
    ANOUNCEMENT_ADD(50, ANOUNCEMENT, "افزودن اعلان"),
    ANOUNCEMENT_LIST(51, ANOUNCEMENT, "لیست اعلان"),
    ANOUNCEMENT_EDIT(52, ANOUNCEMENT, "ویرایش اعلان"),
    ANOUNCEMENT_CALL(53, ANOUNCEMENT, "فراخوانی اعلان"),

    COMMENT(54, ROOT, "دیدگاه"),
    COMMENT_ADD(55, COMMENT, "افزودن دیدگاه"),
    COMMENT_LIST(56, COMMENT, "لیست دیدگاه"),
    COMMENT_EDIT(57, COMMENT, "ویرایش دیدگاه"),

    ORGANIZATION(58, BASIC_DATA, "سازمان"),
    ORGANIZATION_ADD(59, ORGANIZATION, "افزودن سازمان"),
    ORGANIZATION_LIST(60, ORGANIZATION, "لیست سازمان"),
    ORGANIZATION_EDIT(61, ORGANIZATION, "ویرایش سازمان"),

    ENVIRONMENT(62, BASIC_DATA, "محیط اجرایی"),
    ENVIRONMENT_ADD(63, ENVIRONMENT, "افزودن محیط اجرایی"),
    ENVIRONMENT_LIST(64, ENVIRONMENT, "لیست محیط اجرایی"),
    ENVIRONMENT_EDIT(65, ENVIRONMENT, "ویرایش محیط اجرایی"),

    APP_REMOVE(66, APP_MANAGEMENT, "حذف برنامه"),
    Edit_APP_PACKAGE(67, APP_MANAGEMENT, "ویرایش بسته"),

    TIME_LINE(73, BASIC_DATA, "تایم لاین"),
    TIME_LINE_ADD(74, TIME_LINE, "افزودن تایم لاین"),
    TIME_LINE_LIST(75, TIME_LINE, "لیست تایم لاین"),
    TIME_LINE_EDIT(76, TIME_LINE, "ویرایش تایم لاین"),

    HANIJAFARNEZHAD(77, ROOT, "هانی جعفرنژاد"),
    HANI(78, HANIJAFARNEZHAD, "هانی جعفرنژاد"),
// any added access to basic_data should be added in menuForUser Service



// any added access to basic_data should be added in menuForUser Service

    ;

//    EXPEL_USER(16, USER_MANAGEMENT, "اخراج کاربر");

    private static final Logger logger = Logger.getLogger(Access.class);
    private static final Map<Integer, Access> bit2access = Collections.synchronizedMap(new HashMap<Integer, Access>());

    private Integer bitNo;
    private Access parent;
    private String name;
    private List<Access> children = Collections.synchronizedList(new ArrayList<Access>());
    private boolean doLog = true;
    private boolean enabled = true;


    Access(String name) {
        this.name = name;
    }

    Access(Access parent, String name) {
        this.name = name;
        this.parent = parent;
    }

    Access(Integer bitNo, Access parent, String name) {
        this.bitNo = bitNo;
        this.parent = parent;
        this.name = name;
    }

    Access(Integer bitNo, Access parent, String name, boolean doLog) {
        this.bitNo = bitNo;
        this.parent = parent;
        this.name = name;
        this.doLog = doLog;
    }

    Access(Integer bitNo, Access parent, String name, boolean doLog, boolean enabled) {
        this.bitNo = bitNo;
        this.parent = parent;
        this.name = name;
        this.doLog = doLog;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public Integer getBitNo() {
        return bitNo;
    }

    public Access getParent() {
        return parent;
    }

    public List<Access> getChildren() {
        return children;
    }

    public boolean isDoLog() {
        return doLog;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return name;
    }

    public static void init() {
        HashSet<Integer> bitSet = new HashSet<Integer>();
        String disables = ConfigUtil.getProperty(ConfigUtil.APP_DISABLE_ACCESS);
        /*if (disables == null)
              return;*/
        int max = Integer.MIN_VALUE;

        for (Access access : Access.values()) {
            if (access.bitNo == null) {
                access.bitNo = access.ordinal();
                access.doLog = false;
                logger.warn(String.format("Undefined bit no for [%s]", access.name()));
            }
            if (bitSet.contains(access.bitNo))
                throw new AppStoreRuntimeException(String.format("Duplicate bitNo for [%s]", access.name));
            else {
                bitSet.add(access.bitNo);
                if (access.bitNo > max)
                    max = access.bitNo;
            }

            if (access.parent != null)
                access.parent.children.add(access);
            bit2access.put(access.bitNo, access);
            if (disables != null) {
                String[] dis = disables.split(";");
                for (String di : dis) {
                    if (access.name().matches(di)) {
                        access.enabled = false;
                        logger.debug("Access Disabled: " + access.name());
                        break;
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder("Free bit no: ");
            for (int i = 0; i <= max; i++)
                if (!bitSet.contains(i))
                    builder.append(i).append(",");
            logger.debug(builder.toString());
        }
    }

    public static Access findByBitNo(int bitNo) {
        return bit2access.get(bitNo);
    }

    public static Set<TreeNode> getAccessesAsTreeNodes(byte[] bytes) {
        Set<TreeNode> retSet = new HashSet<TreeNode>();
        getAccessesAsTreeNodes(bit2access.values(), null, retSet);

        if (bytes != null && !(bytes.length == 0)) {
            List<Integer> bits = new ArrayList<Integer>();
            if (bytes != null)
                bits = byte2Bit(bytes);
            for (TreeNode treeNode : retSet)
                if (bits.contains(treeNode.getId().intValue())) {
                    treeNode.setSelected(true);
                }
        }
        return retSet;
    }

    private static void getAccessesAsTreeNodes(Collection<Access> accesses, TreeNode parent, Set<TreeNode> retSet) {
        for (Access intendedAccess : accesses) {
            if (intendedAccess.bitNo == -1)
                continue;
            TreeNode nodeToAdd = new TreeNode();
            nodeToAdd.setTitle(intendedAccess.name);
            nodeToAdd.setId((long) intendedAccess.bitNo);
            if (parent != null)
                parent.addChild(nodeToAdd);
            retSet.add(nodeToAdd);
            if (intendedAccess.children != null && !intendedAccess.children.isEmpty()) {
                getAccessesAsTreeNodes(intendedAccess.children, nodeToAdd, retSet);
            }
        }
    }

    private static List<Integer> byte2Bit(byte[] bytes) {
        List<Integer> bits = new ArrayList<Integer>();
        if (bytes != null)
            for (int i = bytes.length - 1; i >= 0; i--) {
                for (int j = 7; j >= 0; j--)
                    if ((bytes[i] & BITS[j]) != 0)
                        bits.add(i * 8 + j);
            }
        return bits;
    }

    private static final byte[] BITS = {1, 2, 4, 8, 16, 32, 64, (byte) 128};

}

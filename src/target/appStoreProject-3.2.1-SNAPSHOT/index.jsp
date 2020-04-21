<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<%! String fanapSSOToken; %>
<%! String generalErroInPortal; %>
<%! String userStatusError; %>
<head>
    <meta charset="UTF-8">

    <title>FanRP Single Sign On Interface</title>

    <link rel="stylesheet" href="css/bootstrap.css">
    <link rel="stylesheet" href="css/ssoErrorPage.css">
    <link rel="stylesheet" href="assets/css/packageListStyles.css"/>
    <link rel="stylesheet" href="assets/css/custom.css"/>
    <link rel="stylesheet" href="assets/css/font-icons/font-awesome/css/font-awesome.css"/>
    <link rel="stylesheet" href="assets/css/cards.css"/>

    <script src="js/bootstrap.js"></script>
    <%
        fanapSSOToken = (String) request.getSession().getAttribute("fanapSSOToken");
        generalErroInPortal = (String) request.getSession().getAttribute("generalErroInPortal");
        userStatusError = (String) request.getSession().getAttribute("userStatusError");
        if (fanapSSOToken != null && !fanapSSOToken.equals("null")) {%>
    <script>
        window.location = '<%=request.getSession().getAttribute("application_path")%>loginPage?fanapSSOToken=<%=URLEncoder.encode(fanapSSOToken)%>';
    </script>
    <%}%>
</head>
<body style="background: #AB47BC;">
<div class="container">
    <div class="row" id="pwd-container">
        <div class="col-md-4"></div>
        <h1 class="col-md-6">
            <% if (generalErroInPortal != null && !generalErroInPortal.equals("null")) { %>
            <form class="formDialog">
                <div>
                    <div>
                        <h1 dir="rtl" style="font-size: 20px;text-align: center;margin-bottom: 40px;">
                            <%= request.getSession().getAttribute("generalErroInPortal") %>
                        </h1>
                    </div>
                </div>
            </form>
            <% } else  if (userStatusError != null && !userStatusError.equals("null")) { %>
            <form class="formDialog">
                <div>
                    <div>
                        <h1 dir="rtl" style="font-size: 20px;text-align: center;margin-bottom: 40px;">
                            <%= request.getSession().getAttribute("userStatusError") %>
                        </h1>
                    </div>
                    <div>
                        <button id="btnCloseInUserStatus" type="button" class="btn btn-default" data-dismiss="modal" dir="rtl"
                                onclick="window.location.href='<%= request.getSession().getAttribute("logoutUser")%>'">
                            <%= request.getSession().getAttribute("btnCloseLabel") %>
                        </button>

                    </div>
                </div>
            </form>

            <% } else { %>
            <form class="formDialog" style="margin-top: 100px;">
                <div>
                    <div>
                        <h1 dir="rtl" style="font-size: 20px;text-align: center;margin-bottom: 40px;">
                            <%= request.getSession().getAttribute("userNameError") %>
                        </h1>
                    </div>
                    <div>
                        <button id="btnClose" type="button" class="btn btn-default" data-dismiss="modal" dir="rtl"
                                onclick="window.location.href='<%= request.getSession().getAttribute("logoutUser")%>'">
                            <%= request.getSession().getAttribute("btnCloseLabel") %>
                        </button>

                    </div>
                </div>
            </form>
            <% } %>
        </h1>
        <div class="col-md-4"></div>
    </div>
</div>
</body>

</html>
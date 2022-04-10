package com.ljq.javaweb.try1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class Login extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        int SpiderState = 0;
        try {
            User_Agent_Check uAC = new User_Agent_Check(request.getHeader("user-agent"));
            Referer_Check RC = new Referer_Check(request.getHeader("referer"), "/servlet02/login.html");
            if (uAC.check()) {
                response.sendRedirect("403.html");//user_agent
                SpiderState = 1;
            } else if (RC.check()) {
                response.sendRedirect("403.html");//referer
                SpiderState = 1;
            }
        } catch (NullPointerException e) {
            response.sendRedirect("403.html");
            SpiderState = 1;
        }
        if (SpiderState==0) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String path = this.getServletContext().getRealPath("/WEB-INF/classes/DB_Info.properties");
                Username_Check check = new Username_Check(username, path);
                if (check.check()) {
                    DB_Connect connect = new DB_Connect();
                    conn = connect.connect(path);
                    String sql = "select*from userinfo where username=? and password=?";//?是占位符
                    ps = conn.prepareStatement(sql);
                    //给占位符？传值，第一个问号下标是1，jdbc的下标从1开始
                    ps.setString(1, username);
                    ps.setString(2, password);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        HttpSession session = request.getSession();
                        if (!session.isNew()) {  //如果session不是新的，那么失效上一个session并再次创建
                            session.invalidate();
                            session = request.getSession();
                        }
                        session.setMaxInactiveInterval(60);//session时长设置为5分钟
                        session.setAttribute("username", username);
                        response.sendRedirect("SUI");
                        //request.getRequestDispatcher("SUI").forward(request,response);
                    } else {
                        response.sendRedirect("wrongpassword.html"); //重定向到密码错误页面
                    }
                } else {
                    response.sendRedirect("wrongusername.html"); //重定向到账号错误页面
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

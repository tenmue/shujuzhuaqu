package com.softyang.sjzq;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 行政区域基础数据抓取
 * @author yancy
 * http://www.stats.gov.cn/tjsj/
 * http://www.cnblogs.com/yangzhilong/p/3530700.html
 *
 */
public class ReadCodeFromWeb {

    public static String baseUrl = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2015/";

    public static StringBuffer result = new StringBuffer();

    public static Connection conn = ConnectionUtil.getConnection();

    public static void main(String[] args) throws Exception {
        readProvince();
    }

    /**
     * 读取省份信息
     * @throws Exception
     */
    public static void readProvince() throws Exception{
        String url = baseUrl + "index.html";
        String str = getContent(url).toUpperCase();
        String[] arrs = str.split("<A");

        for (String s : arrs) {
            if (s.indexOf("HREF") != -1 && s.indexOf(".HTML") != -1) {

                String a = s.substring(7, s.indexOf("'>"));
                String shengFenCode = a.substring(0, 2);
                String name = s.substring(s.indexOf("'>")+2, s.indexOf("<BR/>"));

                File file = new File("D:/baseData/"+name+".html");
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream,"GB2312");
                writer.write("<html><body><table border='1' bordercolor='#000000' style='border-collapse:collapse'><tr><td>代码</td><td>省</td><td>市</td><td>县</td><td>镇</td><td>城乡分类</td><td>村/街道</td></tr>");
                writer.write("<tr><td></td><td>");
                writer.write(name);
                writer.write("</td><td></td><td></td><td></td><td></td><td></td></tr>");

                //saveShengFen(shengFenCode, name);

                readShi(a,writer,shengFenCode);

                writer.write("</table></body></html>");
                writer.flush();
                writer.close();
            }
        }
    }

    /**
     * 读市的数据
     * @throws Exception
     */
    public static void readShi(String url,OutputStreamWriter writer,String shengFenCode) throws Exception{
        String content = getContent(baseUrl+url).toUpperCase();
        String[] citys = content.split("CITYTR");
        //'><TD><A HREF='11/1101.HTML'>110100000000</A></TD><TD><A HREF='11/1101.HTML'>市辖区</A></TD></td><TR CLASS='
        for(int c=1,len=citys.length; c<len; c++){
            String[] strs = citys[c].split("<A HREF='");
            String cityUrl = null;
            String cityCode = null;
            String name = null;
            for(int si = 1; si<3; si++){
                if(si==1){//取链接和编码
                    cityUrl = strs[si].substring(0, strs[si].indexOf("'>"));
                    cityCode = strs[si].substring(strs[si].indexOf("'>")+2, strs[si].indexOf("</A>"));

                    writer.write("<tr><td>");
                    writer.write(cityCode);
                    writer.write("</td>");
                }else{
                    writer.write("<td></td><td>");
                    writer.write(strs[si].substring(strs[si].indexOf("'>") + 2, strs[si].indexOf("</A>")));
                    writer.write("</td><td></td><td></td><td></td><td></td></tr>");
                    name = strs[si].substring(strs[si].indexOf("'>")+2, strs[si].indexOf("</A>"));
                    System.out.println("爬取:"+name);
                }
            }

            /*if(name != null){
            	saveCity(cityCode, shengFenCode, name);
            	name = null;
            }*/

            readXian(cityUrl.substring(0, cityUrl.indexOf("/")+1),cityUrl,writer,cityCode);
        }
    }

    /**
     * 读县的数据
     * @param url
     * @throws Exception
     */
    public static void readXian(String prix,String url,OutputStreamWriter writer,String cityCode) throws Exception{
        String content = getContent(baseUrl+url).toUpperCase();
        String[] citys = content.split("COUNTYTR");
        for(int i=1; i<citys.length; i++){
            String cityUrl = null;
            String code = null;
            String name = null;
            //发现石家庄有一个县居然没超链接，特殊处理
            if(citys[i].indexOf("<A HREF='")==-1){
                writer.write("<tr><td>");
                code = citys[i].substring(6, 18);
                writer.write(code);
                writer.write("</td>");

                writer.write("<td></td><td></td><td>");
                name = citys[i].substring(citys[i].indexOf("</TD><TD>")+9,citys[i].lastIndexOf("</TD>"));
                writer.write(name);
                writer.write("</td><td></td><td></td><td></td></tr>");
            }else{
                String[] strs = citys[i].split("<A HREF='");
                for(int si = 1; si<3; si++){
                    if(si==1){//取链接和编码
                        cityUrl = strs[si].substring(0, strs[si].indexOf("'>"));
                        code = strs[si].substring(strs[si].indexOf("'>")+2, strs[si].indexOf("</A>"));

                        writer.write("<tr><td>");
                        writer.write(code);
                        writer.write("</td>");
                    }else{
                        writer.write("<td></td><td></td><td>");
                        name = strs[si].substring(strs[si].indexOf("'>")+2, strs[si].indexOf("</A>"));
                        writer.write(name);
                        writer.write("</td><td></td><td></td><td></td></tr>");
                    }
                }
            }

            /*if(name != null){
            	saveCounty(cityCode, code, name);
            	name = null;
            }*/

            if(null!=cityUrl){
                readZhen(prix,cityUrl,writer);
            }
        }
    }

    /**
     * 读镇的数据
     * @param url
     * @throws Exception
     */
    public static void readZhen(String prix,String url,OutputStreamWriter writer) throws Exception{
        String content = getContent(baseUrl+prix+url).toUpperCase();
        String myPrix = (prix+url).substring(0, (prix+url).lastIndexOf("/")+1);
        String[] citys = content.split("TOWNTR");
        for(int i=1; i<citys.length; i++){
            String[] strs = citys[i].split("<A HREF='");
            String cityUrl = null;
            for(int si = 1; si<3; si++){
                if(si==1){//取链接和编码
                    cityUrl = strs[si].substring(0, strs[si].indexOf("'>"));
                    String cityCode = strs[si].substring(strs[si].indexOf("'>")+2, strs[si].indexOf("</A>"));

                    writer.write("<tr><td>");
                    writer.write(cityCode);
                    writer.write("</td>");
                }else{
                    writer.write("<td></td><td></td><td></td><td>");
                    writer.write(strs[si].substring(strs[si].indexOf("'>") + 2, strs[si].indexOf("</A>")));
                    writer.write("</td><td></td><td></td></tr>");
                }
            }
            readCun(myPrix,cityUrl,writer);
        }
    }

    /**
     * 读村/街道的数据
     * @param url
     * @throws Exception
     */
    public static void readCun(String prix,String url,OutputStreamWriter writer) throws Exception{
        String content = getContent(baseUrl+prix+url).toUpperCase();
        String[] citys = content.split("VILLAGETR");
        for(int i=1; i<citys.length; i++){
            String[] strs = citys[i].split("<TD>");

            writer.write("<tr><td>");
            writer.write(strs[1].substring(0, strs[1].indexOf("</TD>")));
            writer.write("</td>");

            writer.write("<td></td><td></td><td></td><td></td>");
            writer.write("<td>");
            writer.write(strs[2].substring(0, strs[2].indexOf("</TD>")));
            writer.write("</td><td>");
            writer.write(strs[3].substring(0, strs[3].indexOf("</TD>")));
            writer.write("</td></tr>");
        }
    }

    //获取网页的内容
    public static String getContent(String strUrl) throws Exception {
        try {
            URL url = new URL(strUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(),"GB2312"));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }

            br.close();
            return sb.toString();
        } catch (Exception e) {
            System.out.println("can't open url:"+strUrl);
            throw e;
        }
    }

    /**
     * 保存省份信息到数据库
     * @param code
     * @param name
     */
    public static void saveShengFen(String code, String name){
        String sql = "insert into tb_province(province_code, province_name) values(?,?)";
        PreparedStatement statement;
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, code);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存城市信息到数据库
     * @param cityCode
     * @param provinceCode
     * @param cityName
     */
    public static void saveCity(String cityCode, String provinceCode, String cityName){
        String sql = "insert into tb_city(city_code, province_code, city_name) values(?,?,?)";
        PreparedStatement statement;
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, cityCode);
            statement.setString(2, provinceCode);
            statement.setString(3, cityName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存县数据到数据库
     * @param cityCode
     * @param countyCode
     * @param countyName
     */
    public static void saveCounty(String cityCode, String countyCode, String countyName){
        String sql = "insert into tb_county(city_code, county_code, county_name) values(?,?,?)";
        PreparedStatement statement;
        try {
            statement = conn.prepareStatement(sql);
            statement.setString(1, cityCode);
            statement.setString(2, countyCode);
            statement.setString(3, countyName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

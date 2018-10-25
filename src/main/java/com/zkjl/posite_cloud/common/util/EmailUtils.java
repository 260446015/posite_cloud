package com.zkjl.posite_cloud.common.util;

import com.alibaba.fastjson.JSONObject;
import com.zkjl.posite_cloud.domain.pojo.JobInfo;
import com.zkjl.posite_cloud.domain.pojo.User;
import com.zkjl.posite_cloud.exception.CustomerException;
import org.apache.commons.lang3.StringUtils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author yindawei
 * @date 2018/8/15 19:50
 **/
public class EmailUtils {

    public static void sendEamil(JSONObject data, User user) throws Exception {
        if(StringUtils.isBlank(user.getEmail())){
            throw new CustomerException("用户未填写邮箱");
        }
        Properties prop = new Properties();
        prop.setProperty("mail.host", "smtp.mxhichina.com");
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");
        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        //2、通过session得到transport对象
        Transport ts = session.getTransport();
        //3、连上邮件服务器
        ts.connect("smtp.mxhichina.com", "zkjl@zkjldata.com", "ZHONGKEJINLIAN@2018");
        //4、创建邮件
        Message message = createAttachMail(session, data, user);
        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
    }

    /**
     * * @Method: createAttachMail
     * * @Description: 创建一封带附件的邮件
     * * @param session
     * * @return
     * * @throws Exception
     */
    private static MimeMessage createAttachMail(Session session, JSONObject data, User user) throws Exception {
        MimeMessage message = new MimeMessage(session);

        //设置邮件的基本信息
        String nick="";
        try {
            nick=javax.mail.internet.MimeUtility.encodeText("中科金联（北京）科技有限公司");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //发件人
        message.setFrom(new InternetAddress(nick+" <zkjl@zkjldata.com>"));
        //收件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
        //邮件标题
//        message.setSubject("JavaMail邮件发送测试");
        message.setSubject(data.getString("title"));

        //创建邮件正文，为了避免邮件正文中文乱码问题，需要使用charset=UTF-8指明字符编码
        MimeBodyPart text = new MimeBodyPart();
//        text.setContent("使用JavaMail创建的带附件的邮件", "text/html;charset=UTF-8");
        text.setContent(data.getString("content"), "text/html;charset=UTF-8");
//        MimeBodyPart attach = new MimeBodyPart();
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(text);
        message.setContent(mp);
        /*//创建邮件附件
        MimeBodyPart attach = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource("src\\2.jpg"));
        attach.setDataHandler(dh);
        attach.setFileName(dh.getName());  //

        //创建容器描述数据关系
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(text);
        mp.addBodyPart(attach);
        mp.setSubType("mixed");

        message.setContent(mp);
        message.saveChanges();
        //将创建的Email写入到E盘存储
        message.writeTo(new FileOutputStream("E:\\attachMail.eml"));*/
        //返回生成的邮件
        return message;
    }

    public static JSONObject preSendEmail(JobInfo data, int totalSorce) {
        List<String> webname = data.getData().stream().map(action -> {
            JSONObject target = new JSONObject((Map<String, Object>) action);
            return target.getString("webname");
        }).collect(Collectors.toList());
        String plat = StringUtils.join(webname, ",");
        String date = data.getCreationTime();
        JSONObject param = new JSONObject();
        param.put("title", "重点人网络筛查平台积分预警");
        param.put("content", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;尊敬的用户您好，手机号码" + data.getMobile() + "积分达到" + totalSorce + "分，系统研判为红色预警。注册违法平台有" + plat + "。采集时间为" + date + "。");
        return param;
    }
}

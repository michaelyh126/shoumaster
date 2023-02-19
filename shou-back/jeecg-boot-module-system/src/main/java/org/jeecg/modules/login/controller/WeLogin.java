package org.jeecg.modules.login.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.demo.student_check.service.IStudentCheckService;
import org.jeecg.modules.demo.student_check.service.impl.StudentCheckServiceImpl;
import org.jeecg.modules.demo.student_records.service.IStudentRecordsService;
import org.jeecg.modules.system.controller.LoginController;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "openid")
@CrossOrigin
public class WeLogin {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private IStudentCheckService studentCheckService;
    @Autowired
    private IStudentRecordsService studentRecordsService;
    @Resource
    private BaseCommonService baseCommonService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysTenantService sysTenantService;
    @Autowired
    private ISysDictService sysDictService;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Result<JSONObject> VoiceprintRegister(@RequestBody JSONObject jsonObject) throws IOException {
        Result<JSONObject> result = new Result<JSONObject>();
        SysUser user=new SysUser();
        String username = jsonObject.getString("username");
        //未设置密码，则随机生成一个密码
        String password = jsonObject.getString("password");

        if(oConvertUtils.isEmpty(password)){
            password = RandomUtil.randomString(8);
        }
        SysUser sysUser1 = sysUserService.getUserByName(username);
        if (sysUser1 != null) {
            result.setMessage("用户名已注册");
            result.setSuccess(false);
            return result;
        }
        try {
            user.setCreateTime(new Date());// 设置创建时间
            String salt = oConvertUtils.randomGen(8);
            String passwordEncode = PasswordUtil.encrypt(username, password, salt);
            user.setSalt(salt);
            user.setUsername(username);
            user.setRealname(username);
            user.setPassword(passwordEncode);
            user.setStatus(CommonConstant.USER_UNFREEZE);
            user.setDelFlag(CommonConstant.DEL_FLAG_0);
            user.setActivitiSync(CommonConstant.ACT_SYNC_0);
            sysUserService.addUserWithRole(user, "ee8626f80f7c2619917b6236f3a7f02b");
            result.success("注册成功");
        } catch (Exception e) {
            result.error500("注册失败");
        }
        return result;
    }



    @RequestMapping(value = "/VoiceprintLogin", method = RequestMethod.POST)
    public Result<JSONObject> VoiceprintLogin(@RequestBody MultipartFile file) throws IOException {
        Result<JSONObject> result = new Result<JSONObject>();
        boolean ifFileExist=saveFile(file);
        if(ifFileExist==false){
            return result.error500("文件保存出错");
        }
//        String url="http://127.0.0.1:5000/voiceprint";
        String url="https://6fd6-183-193-155-185.jp.ngrok.io/voiceprint";
        HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection();
        //可以设置很多属性
        conn.setRequestMethod("POST");
//        conn.setReadTimeout(30000);
//        conn.setConnectTimeout(100000);
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            System.out.println(inputStream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            String line;
            line = reader.readLine();
            reader.close();
            //该干的都干完了,记得把连接断了
            conn.disconnect();
        if(line.equals("NotExist")){return result.error500("声纹未注册");}
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername,line);
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        result = sysUserService.checkUserIsEffective(sysUser);
        if(!result.isSuccess()) { return result; }
        userInfo(sysUser, result);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        baseCommonService.addLog("用户名: " + line + ",登录成功！", CommonConstant.LOG_TYPE_1, null,loginUser);



        }

        return result;
    }


    @RequestMapping("/book")
    public String book(){
        return "book";
    }

    @RequestMapping("/testopenid")
    public String getUserInfo(@RequestParam(name = "code") String code,String MyLongitude,String MyLatitude,String distance) throws Exception {

        String url = "https://api.weixin.qq.com/sns/jscode2session";
        url += "?appid=wxb47486e034972543";//自己的appid
        url += "&secret=63c7b43dc4123badc149ddc07babd367";//自己的appSecret
        url += "&js_code=" + code;
        url += "&grant_type=authorization_code";
        url += "&connect_redirect=1";
        String res = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);    //GET方式
        CloseableHttpResponse response = null;
        // 配置信息
        RequestConfig requestConfig = RequestConfig.custom()          // 设置连接超时时间(单位毫秒)
                .setConnectTimeout(5000)                    // 设置请求超时时间(单位毫秒)
                .setConnectionRequestTimeout(5000)             // socket读写超时时间(单位毫秒)
                .setSocketTimeout(5000)                    // 设置是否允许重定向(默认为true)
                .setRedirectsEnabled(false).build();           // 将上面的配置信息 运用到这个Get请求里
        httpget.setConfig(requestConfig);                         // 由客户端执行(发送)Get请求
        response = httpClient.execute(httpget);                   // 从响应模型中获取响应实体
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            res = EntityUtils.toString(responseEntity);

        }
        // 释放资源
        if (httpClient != null) {
            httpClient.close();
        }
        if (response != null) {
            response.close();
        }
        JSONObject jo = JSON.parseObject(res);
        String openid = jo.getString("openid");
        boolean ifopenid=studentCheckService.IfOpenid(openid);
        if(ifopenid==false){return openid;}
        studentRecordsService.insert(openid,MyLongitude,MyLatitude,distance);
        return "true";
    }

    //首次登记并签到
    @RequestMapping("/InsertStudentCheck")
    public Result<String> insertStudentCheck(@RequestParam(name = "name") String name, String no, String phone, String openid, String state,
  String MyLongitude,String MyLatitude,String distance,String community  ) throws Exception {
        if(state.equals("1")){state="1468387957525233665";}
        else{state="1468388126513741825";}
        studentCheckService.insert(name,no,phone,openid,state,community);
        studentRecordsService.firstInsert(name,no,phone,openid,state,MyLongitude,MyLatitude,distance,community);
        Result<String> result=new Result();
        result.setSuccess(true);
        result.setResult("成功");
        return result;
    }

    public boolean saveFile(MultipartFile file){
        if (file.isEmpty()) {
            return false;
        }
        String fileName = file.getOriginalFilename();
//        File dest = new File(new File("/usr/local/jeecgboot/audio").getAbsolutePath()+ "/" + fileName);
        File dest = new File(new File("C:\\Users\\86139\\Desktop\\VoiceprintRecognition-Pytorch\\audio").getAbsolutePath()+ "/" + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest); // 保存文件
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 用户信息
     *
     * @param sysUser
     * @param result
     * @return
     */
    public Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
        String syspassword = sysUser.getPassword();
        String username = sysUser.getUsername();
        // 获取用户部门信息
        JSONObject obj = new JSONObject();
        java.util.List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
        obj.put("departs", departs);
        if (departs == null || departs.size() == 0) {
            obj.put("multi_depart", 0);
        } else if (departs.size() == 1) {
            sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            obj.put("multi_depart", 1);
        } else {
            //查询当前是否有登录部门
            // update-begin--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            SysUser sysUserById = sysUserService.getById(sysUser.getId());
            if(oConvertUtils.isEmpty(sysUserById.getOrgCode())){
                sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            }
            // update-end--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
            obj.put("multi_depart", 2);
        }
        // update-begin--Author:sunjianlei Date:20210802 for：获取用户租户信息
        String tenantIds = sysUser.getRelTenantIds();
        if (oConvertUtils.isNotEmpty(tenantIds)) {
            java.util.List<Integer> tenantIdList = new ArrayList<>();
            for(String id: tenantIds.split(",")){
                tenantIdList.add(Integer.valueOf(id));
            }
            // 该方法仅查询有效的租户，如果返回0个就说明所有的租户均无效。
            List<SysTenant> tenantList = sysTenantService.queryEffectiveTenant(tenantIdList);
            if (tenantList.size() == 0) {
                result.error500("与该用户关联的租户均已被冻结，无法登录！");
                return result;
            } else {
                obj.put("tenantList", tenantList);
            }
        }
        // update-end--Author:sunjianlei Date:20210802 for：获取用户租户信息
        // 生成token
        String token = JwtUtil.sign(username, syspassword);
        // 设置token缓存有效时间
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
        obj.put("token", token);
        obj.put("userInfo", sysUser);
        obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
        result.setResult(obj);
        result.success("登录成功");
        return result;
    }


    @RequestMapping(value = "/saveAudio", method = RequestMethod.POST)
    public Result<JSONObject> saveAudio(@RequestBody MultipartFile file) throws IOException {
        Result<JSONObject> result = new Result<JSONObject>();
        boolean ifFileExist=false;
        if (file.isEmpty()) {
            return result.error500("文件为空");
        }
        String fileName = file.getOriginalFilename();
//        File dest = new File(new File("/usr/local/jeecgboot/audio_db").getAbsolutePath()+ "/" + fileName);
        File dest = new File(new File("C:\\Users\\86139\\Desktop\\VoiceprintRecognition-Pytorch\\audio_db").getAbsolutePath()+ "/" + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest); // 保存文件
            ifFileExist=true;
            sendTOPython(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return result.error500("error");
        }

        if(ifFileExist==false){
            return result.error500("文件保存出错");
        }
        return Result.ok("保存完毕");
    }


    public void sendTOPython(String param) throws IOException {
        OutputStream out = null;
        String url = "https://6fd6-183-193-155-185.jp.ngrok.io/voiceprintResgister";
        HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection();
        conn.setRequestMethod("POST");
//        conn.setReadTimeout(30000);
//        conn.setConnectTimeout(100000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        out = conn.getOutputStream();
        out.write(param.getBytes());
        out.flush(); //清空缓冲区,发送数据
        out.close();
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            System.out.println(inputStream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            line = reader.readLine();
            reader.close();
            //该干的都干完了,记得把连接断了
            conn.disconnect();
        }
    }



}

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
        //?????????????????????????????????????????????
        String password = jsonObject.getString("password");

        if(oConvertUtils.isEmpty(password)){
            password = RandomUtil.randomString(8);
        }
        SysUser sysUser1 = sysUserService.getUserByName(username);
        if (sysUser1 != null) {
            result.setMessage("??????????????????");
            result.setSuccess(false);
            return result;
        }
        try {
            user.setCreateTime(new Date());// ??????????????????
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
            result.success("????????????");
        } catch (Exception e) {
            result.error500("????????????");
        }
        return result;
    }



    @RequestMapping(value = "/VoiceprintLogin", method = RequestMethod.POST)
    public Result<JSONObject> VoiceprintLogin(@RequestBody MultipartFile file) throws IOException {
        Result<JSONObject> result = new Result<JSONObject>();
        boolean ifFileExist=saveFile(file);
        if(ifFileExist==false){
            return result.error500("??????????????????");
        }
//        String url="http://127.0.0.1:5000/voiceprint";
        String url="https://6fd6-183-193-155-185.jp.ngrok.io/voiceprint";
        HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection();
        //????????????????????????
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
            //?????????????????????,?????????????????????
            conn.disconnect();
        if(line.equals("NotExist")){return result.error500("???????????????");}
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername,line);
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        result = sysUserService.checkUserIsEffective(sysUser);
        if(!result.isSuccess()) { return result; }
        userInfo(sysUser, result);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        baseCommonService.addLog("?????????: " + line + ",???????????????", CommonConstant.LOG_TYPE_1, null,loginUser);



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
        url += "?appid=wxb47486e034972543";//?????????appid
        url += "&secret=63c7b43dc4123badc149ddc07babd367";//?????????appSecret
        url += "&js_code=" + code;
        url += "&grant_type=authorization_code";
        url += "&connect_redirect=1";
        String res = null;
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);    //GET??????
        CloseableHttpResponse response = null;
        // ????????????
        RequestConfig requestConfig = RequestConfig.custom()          // ????????????????????????(????????????)
                .setConnectTimeout(5000)                    // ????????????????????????(????????????)
                .setConnectionRequestTimeout(5000)             // socket??????????????????(????????????)
                .setSocketTimeout(5000)                    // ???????????????????????????(?????????true)
                .setRedirectsEnabled(false).build();           // ???????????????????????? ???????????????Get?????????
        httpget.setConfig(requestConfig);                         // ??????????????????(??????)Get??????
        response = httpClient.execute(httpget);                   // ????????????????????????????????????
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            res = EntityUtils.toString(responseEntity);

        }
        // ????????????
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

    //?????????????????????
    @RequestMapping("/InsertStudentCheck")
    public Result<String> insertStudentCheck(@RequestParam(name = "name") String name, String no, String phone, String openid, String state,
  String MyLongitude,String MyLatitude,String distance  ) throws Exception {
        if(state.equals("1")){state="1468387957525233665";}
        else{state="1468388126513741825";}
        studentCheckService.insert(name,no,phone,openid,state);
        studentRecordsService.firstInsert(name,no,phone,openid,state,MyLongitude,MyLatitude,distance);
        Result<String> result=new Result();
        result.setSuccess(true);
        result.setResult("??????");
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
            file.transferTo(dest); // ????????????
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * ????????????
     *
     * @param sysUser
     * @param result
     * @return
     */
    public Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result) {
        String syspassword = sysUser.getPassword();
        String username = sysUser.getUsername();
        // ????????????????????????
        JSONObject obj = new JSONObject();
        java.util.List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
        obj.put("departs", departs);
        if (departs == null || departs.size() == 0) {
            obj.put("multi_depart", 0);
        } else if (departs.size() == 1) {
            sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            obj.put("multi_depart", 1);
        } else {
            //?????????????????????????????????
            // update-begin--Author:wangshuai Date:20200805 for????????????????????????????????????????????????????????????????????????????????????????????????
            SysUser sysUserById = sysUserService.getById(sysUser.getId());
            if(oConvertUtils.isEmpty(sysUserById.getOrgCode())){
                sysUserService.updateUserDepart(username, departs.get(0).getOrgCode());
            }
            // update-end--Author:wangshuai Date:20200805 for????????????????????????????????????????????????????????????????????????????????????????????????
            obj.put("multi_depart", 2);
        }
        // update-begin--Author:sunjianlei Date:20210802 for???????????????????????????
        String tenantIds = sysUser.getRelTenantIds();
        if (oConvertUtils.isNotEmpty(tenantIds)) {
            java.util.List<Integer> tenantIdList = new ArrayList<>();
            for(String id: tenantIds.split(",")){
                tenantIdList.add(Integer.valueOf(id));
            }
            // ????????????????????????????????????????????????0???????????????????????????????????????
            List<SysTenant> tenantList = sysTenantService.queryEffectiveTenant(tenantIdList);
            if (tenantList.size() == 0) {
                result.error500("????????????????????????????????????????????????????????????");
                return result;
            } else {
                obj.put("tenantList", tenantList);
            }
        }
        // update-end--Author:sunjianlei Date:20210802 for???????????????????????????
        // ??????token
        String token = JwtUtil.sign(username, syspassword);
        // ??????token??????????????????
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
        obj.put("token", token);
        obj.put("userInfo", sysUser);
        obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
        result.setResult(obj);
        result.success("????????????");
        return result;
    }


    @RequestMapping(value = "/saveAudio", method = RequestMethod.POST)
    public Result<JSONObject> saveAudio(@RequestBody MultipartFile file) throws IOException {
        Result<JSONObject> result = new Result<JSONObject>();
        boolean ifFileExist=false;
        if (file.isEmpty()) {
            return result.error500("????????????");
        }
        String fileName = file.getOriginalFilename();
//        File dest = new File(new File("/usr/local/jeecgboot/audio_db").getAbsolutePath()+ "/" + fileName);
        File dest = new File(new File("C:\\Users\\86139\\Desktop\\VoiceprintRecognition-Pytorch\\audio_db").getAbsolutePath()+ "/" + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest); // ????????????
            ifFileExist=true;
            sendTOPython(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return result.error500("error");
        }

        if(ifFileExist==false){
            return result.error500("??????????????????");
        }
        return Result.ok("????????????");
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
        out.flush(); //???????????????,????????????
        out.close();
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            System.out.println(inputStream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            line = reader.readLine();
            reader.close();
            //?????????????????????,?????????????????????
            conn.disconnect();
        }
    }



}

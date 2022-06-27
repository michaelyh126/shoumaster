package org.jeecg.modules.demo.student_check.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.modules.demo.student_check.entity.StudentCheck;
import org.jeecg.modules.demo.student_check.mapper.StudentCheckMapper;
import org.jeecg.modules.demo.student_check.service.IStudentCheckService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 学生登记表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
@Service
public class StudentCheckServiceImpl extends ServiceImpl<StudentCheckMapper, StudentCheck> implements IStudentCheckService {

    public boolean IfOpenid(String openid){
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.select("openid","name","no","phone","community","state","is_exception").eq("openid",openid);
        List<StudentCheck> studentCheckList= baseMapper.selectList(queryWrapper);
        if(studentCheckList.isEmpty()){
            System.out.println("不存在openid");
            return false;
        }
        else{
            System.out.println("存在openid");

            return true;
        }
    }

    @Override
    public void insert(String name, String no, String phone, String openid,String state) {
        StudentCheck studentCheck=new StudentCheck();
        studentCheck.setName(name).setNo(no).setPhone(phone).setOpenid(openid).setState(state).setIsException("0");
        baseMapper.insert(studentCheck);
    }
}

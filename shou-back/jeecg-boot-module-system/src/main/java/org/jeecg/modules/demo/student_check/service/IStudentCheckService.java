package org.jeecg.modules.demo.student_check.service;

import org.jeecg.modules.demo.student_check.entity.StudentCheck;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 学生登记表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
public interface IStudentCheckService extends IService<StudentCheck> {
public boolean IfOpenid(String openid);
public void insert(String name,String no,String phone,String openid,String state,String community);
}

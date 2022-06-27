package org.jeecg.modules.demo.student_records.service;

import org.jeecg.modules.demo.student_records.entity.StudentRecords;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 学生出入记录表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
public interface IStudentRecordsService extends IService<StudentRecords> {
void firstInsert(String name,String no,String phone,String openid,String state,String MyLongitude,String MyLatitude,String distance);
void insert(String openid,String MyLongitude,String MyLatitude,String distance);
void getException();
}

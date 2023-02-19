package org.jeecg.modules.demo.student_records.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.demo.student_check.entity.StudentCheck;
import org.jeecg.modules.demo.student_check.mapper.StudentCheckMapper;
import org.jeecg.modules.demo.student_records.entity.StudentRecords;
import org.jeecg.modules.demo.student_records.mapper.StudentRecordsMapper;
import org.jeecg.modules.demo.student_records.service.IStudentRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Description: 学生出入记录表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
@Service
public class StudentRecordsServiceImpl extends ServiceImpl<StudentRecordsMapper, StudentRecords> implements IStudentRecordsService {

    @Autowired
    private  StudentRecordsMapper studentRecordsMapper;
    @Autowired
    private StudentCheckMapper studentCheckMapper;
    @Override
    public void firstInsert(String name,String no,String phone,String openid,String state,String MyLongitude,String MyLatitude,String distance,String community) {
         StudentRecords studentRecords=new StudentRecords();
         String exception;
         if(state.equals("1468387957525233665")){
             exception="1";
             studentRecords.setName(name).setNo(no).setPhone(phone).setOpenid(openid).setState(state).setIsException(exception).setCommunity(community)
             .setLatitude(MyLatitude).setLongitude(MyLongitude).setDistance(distance);
         }
         else {
             exception="0";
             SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             Date date=new Date();
            Date returnTime= StudentRecordsServiceImpl.addHour(date,1800);
             studentRecords.setName(name).setNo(no).setPhone(phone).setOpenid(openid).setState(state).setIsException(exception)
             .setReturnTime(returnTime).setCommunity(community);
         }

         baseMapper.insert(studentRecords);
    }

    @Override
    @Transactional
    public void insert(String openid,String MyLongitude,String MyLatitude,String distance) {

        StudentRecords studentRecords=new StudentRecords();
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.select("openid","name","no","phone","community","state","is_exception").eq("openid",openid);
        List<StudentCheck> studentCheckList= studentCheckMapper.selectList(queryWrapper);
        StudentCheck studentCheck=studentCheckList.get(0);
        studentRecords.setName(studentCheck.getName())
                .setNo(studentCheck.getNo())
                .setPhone(studentCheck.getPhone())
                .setOpenid(openid)
                .setCommunity(studentCheck.getCommunity())
                .setLongitude(MyLongitude)
                .setLatitude(MyLatitude)
                .setDistance(distance)
                .setSignTime(new Date());

        StudentRecords lastStudentRecord =studentRecordsMapper.getNew();
       String flag= lastStudentRecord.getIsException();
        if(flag.equals("0")){
            studentRecords.setIsException("1");
            studentRecords.setState("1468387957525233665");
            lastStudentRecord.setIsException("1");
            QueryWrapper lastQueryWrapper=new QueryWrapper();
            lastQueryWrapper.eq("aid",lastStudentRecord.getAid());
            studentRecordsMapper.update(lastStudentRecord,lastQueryWrapper);
            studentRecordsMapper.insert(studentRecords);
        }
        if(flag.equals("1")){
            studentRecords.setIsException("0");
            studentRecords.setState("1468388126513741825");
            Date date=new Date();
            Date returnTime= StudentRecordsServiceImpl.addHour(date,1800);
            studentRecords.setReturnTime(returnTime);
            studentRecordsMapper.insert(studentRecords);
        }

    }

    @Override
    public void getException() {
        //判断超时异常
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date();
        List<StudentRecords> studentRecordsList =studentRecordsMapper.out_time_info(date);
        for(StudentRecords o :studentRecordsList) {
            o.setIsException("1").setState("error");
            QueryWrapper queryWrapper=new QueryWrapper();
            queryWrapper.eq("aid",o.getAid());
            studentRecordsMapper.update(o,queryWrapper);
        }


    }

    /**
     * 在原日期的基础上增加小时数
     * @param date
     * @param i
     * @return
     */
    public static Date addHour(Date date,int i){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.SECOND, i);
        Date newDate = c.getTime();
        return newDate;
    }

}

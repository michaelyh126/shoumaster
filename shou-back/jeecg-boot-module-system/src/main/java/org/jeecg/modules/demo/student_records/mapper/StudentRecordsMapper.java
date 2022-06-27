package org.jeecg.modules.demo.student_records.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.demo.student_records.entity.StudentRecords;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 学生出入记录表
 * @Author: jeecg-boot
 * @Date:   2022-05-02
 * @Version: V1.0
 */
public interface StudentRecordsMapper extends BaseMapper<StudentRecords> {
    public StudentRecords getNew();
    public List<StudentRecords> out_time_info(Date date);
}
